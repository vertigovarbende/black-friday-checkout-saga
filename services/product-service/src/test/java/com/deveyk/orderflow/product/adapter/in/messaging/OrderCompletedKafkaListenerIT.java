package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.OrderCompletedEventBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReserveRequestedEventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCompletedKafkaListenerIT extends ProductTestContainerConfiguration {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private StockReservationReadPort stockReservationReadPort;

    @Autowired
    private ProductReadPort productReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("order.completed rezervasyonu kesinleştirir: COMMITTED, onHand ve reserved adet kadar düşer, cevap yazılmaz")
    void givenReservedStock_whenOrderCompleted_thenCommittedWithoutReply() {
        // Given
        final UUID orderId = UUID.randomUUID();
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, new StockReserveRequestedEventBuilder().withValidValues().withOrderId(orderId).build());
        awaitStatus(orderId, ReservationStatus.RESERVED);

        final OrderflowEvent<OrderCompletedPayload> event = new OrderCompletedEventBuilder().withValidValues().withOrderId(orderId).build();

        // When
        messagePublisher.publishEvent(TopicNames.ORDER_COMPLETED, event);

        // Then
        awaitStatus(orderId, ReservationStatus.COMMITTED);
        final Product product = productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow();
        assertThat(product.getOnHand().value())
                .isEqualTo(ProductValidTestData.Product.ON_HAND.value() - ProductValidTestData.Order.QUANTITY.value());
        assertThat(product.getReserved().value()).isZero();

        InboxAssert.of(jdbcTemplate).processedOnce(OrderCompletedKafkaListener.CONSUMER, event.metadata().eventId());
        // Outbox'ta sadece reserve'ün cevabı var: commit saga'ya cevap vermez (05 §8).
        OutboxAssert.of(jdbcTemplate).hasEvents(orderId.toString(), EventDefinitions.STOCK_RESERVED.eventType(), 1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_event WHERE aggregate_id = ?", Integer.class, orderId.toString()))
                .isEqualTo(1);
    }

    private void awaitStatus(final UUID orderId, final ReservationStatus status) {
        Awaitility.await("reservation %s %s".formatted(orderId, status))
                .atMost(TIMEOUT)
                .until(() -> stockReservationReadPort.findById(new OrderId(orderId)),
                        found -> found.filter(reservation -> reservation.getStatus() == status).isPresent());
    }
}
