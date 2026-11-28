package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.StockReleaseRequestedEventBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReserveRequestedEventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.jayway.jsonpath.JsonPath;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockReleaseRequestedKafkaListenerIT extends ProductTestContainerConfiguration {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private StockReservationReadPort stockReservationReadPort;

    @Autowired
    private ProductReadPort productReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Telafi C2: ayrılmış stok geri döner, rezervasyon RELEASED, stock.released yazılır")
    void givenReservedStock_whenReleaseRequested_thenReleasedAndReplied() {
        // Given
        final UUID orderId = UUID.randomUUID();
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, new StockReserveRequestedEventBuilder().withValidValues().withOrderId(orderId).build());
        awaitStatus(orderId, ReservationStatus.RESERVED);

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RELEASE_REQUESTED, new StockReleaseRequestedEventBuilder().withValidValues().withOrderId(orderId).build());

        // Then
        awaitStatus(orderId, ReservationStatus.RELEASED);
        assertThat(productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow().getReserved().value()).isZero();
        assertThat(OutboxAssert.of(jdbcTemplate).hasEvent(orderId.toString(), EventDefinitions.STOCK_RELEASED.eventType()).topic())
                .isEqualTo(TopicNames.STOCK_RELEASED);
    }

    @Test
    @DisplayName("Reserve'den önce gelen release kapanış kaydı açar; sonra gelen reserve RESERVATION_CLOSED ile reddedilir, stoğa dokunmaz")
    void givenReleaseBeforeReserve_whenReserveArrivesLate_thenReservationClosed() {
        // Given
        final UUID orderId = UUID.randomUUID();
        messagePublisher.publishEvent(TopicNames.STOCK_RELEASE_REQUESTED, new StockReleaseRequestedEventBuilder().withValidValues().withOrderId(orderId).build());
        final StockReservation closed = awaitStatus(orderId, ReservationStatus.RELEASED);
        assertThat(closed.isClosingRecord()).isTrue();
        OutboxAssert.of(jdbcTemplate).hasEvent(orderId.toString(), EventDefinitions.STOCK_RELEASED.eventType());

        final OrderflowEvent<StockReserveRequestedPayload> lateReserve = new StockReserveRequestedEventBuilder().withValidValues().withOrderId(orderId).build();

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, lateReserve);

        // Then
        Awaitility.await("late reserve processed").atMost(TIMEOUT).untilAsserted(() ->
                InboxAssert.of(jdbcTemplate).processedOnce(StockReserveRequestedKafkaListener.CONSUMER, lateReserve.metadata().eventId()));

        final OutboxAssert.OutboxRow reply = OutboxAssert.of(jdbcTemplate).hasEvent(orderId.toString(), EventDefinitions.STOCK_RESERVE_FAILED.eventType());
        assertThat(JsonPath.<String>read(reply.payload(), "$.payload.reason")).isEqualTo(RejectionReason.RESERVATION_CLOSED.name());
        assertThat(JsonPath.<Object>read(reply.payload(), "$.payload.productId")).isNull();

        assertThat(stockReservationReadPort.findById(new OrderId(orderId)).orElseThrow().getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow().getReserved().value()).isZero();
    }

    private StockReservation awaitStatus(final UUID orderId, final ReservationStatus status) {
        return Awaitility.await("reservation %s %s".formatted(orderId, status))
                .atMost(TIMEOUT)
                .until(() -> stockReservationReadPort.findById(new OrderId(orderId)),
                        found -> found.filter(reservation -> reservation.getStatus() == status).isPresent())
                .orElseThrow();
    }
}
