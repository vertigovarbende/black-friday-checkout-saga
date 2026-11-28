package com.deveyk.orderflow.product.adapter.in.messaging;

import ch.qos.logback.classic.Level;
import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.StockReserveRequestedEventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import com.deveyk.orderflow.shared.testsupport.messaging.MessageProbe;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.jayway.jsonpath.JsonPath;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockReserveRequestedKafkaListenerIT extends ProductTestContainerConfiguration {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private StockReservationReadPort stockReservationReadPort;

    @Autowired
    private ProductReadPort productReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Yeterli stok: rezervasyon RESERVED, ürünün reserved'ı artar, stock.reserved outbox'a ve mesaj inbox'a yazılır")
    void givenEnoughStock_whenReserveRequested_thenReservedAndReplied() {
        // Given
        final OrderflowEvent<StockReserveRequestedPayload> event = new StockReserveRequestedEventBuilder().withValidValues().build();
        final OrderId orderId = new OrderId(event.payload().orderId());

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, event);

        // Then
        final StockReservation reservation = awaitReservation(orderId);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservedOf()).isEqualTo(ProductValidTestData.Order.QUANTITY.value());

        final OutboxAssert.OutboxRow reply = OutboxAssert.of(jdbcTemplate).hasEvent(orderId.value().toString(), EventDefinitions.STOCK_RESERVED.eventType());
        assertThat(reply.topic()).isEqualTo(TopicNames.STOCK_RESERVED);
        assertThat(JsonPath.<String>read(reply.payload(), "$.payload.orderId")).isEqualTo(orderId.value().toString());

        final InboxAssert.InboxRow inbox = InboxAssert.of(jdbcTemplate).processedOnce(StockReserveRequestedKafkaListener.CONSUMER, event.metadata().eventId());
        assertThat(inbox.eventType()).isEqualTo(EventDefinitions.STOCK_RESERVE_REQUESTED.eventType());
    }

    @Test
    @DisplayName("Yetersiz stok: rezervasyon REJECTED, stoğa dokunulmaz, stock.reserve.failed INSUFFICIENT_STOCK ve ürünüyle yazılır")
    void givenInsufficientStock_whenReserveRequested_thenRejectedAndFailedReplied() {
        // Given
        final int moreThanOnHand = ProductValidTestData.Product.ON_HAND.value() + 1;
        final OrderflowEvent<StockReserveRequestedPayload> event = new StockReserveRequestedEventBuilder().withValidValues()
                .withLines(List.of(StockReserveRequestedEventBuilder.line(ProductValidTestData.Product.ID, moreThanOnHand,
                        MoneyAmount.of(ProductValidTestData.Product.UNIT_PRICE, ProductValidTestData.TRY))))
                .build();
        final OrderId orderId = new OrderId(event.payload().orderId());

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, event);

        // Then
        assertThat(awaitReservation(orderId).getStatus()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(reservedOf()).isZero();

        final OutboxAssert.OutboxRow reply = OutboxAssert.of(jdbcTemplate).hasEvent(orderId.value().toString(), EventDefinitions.STOCK_RESERVE_FAILED.eventType());
        assertThat(JsonPath.<String>read(reply.payload(), "$.payload.reason")).isEqualTo(RejectionReason.INSUFFICIENT_STOCK.name());
        assertThat(JsonPath.<String>read(reply.payload(), "$.payload.productId")).isEqualTo(ProductValidTestData.Product.ID.toString());
    }

    @Test
    @DisplayName("Aynı mesaj (aynı eventId) iki kez gelir: ikincisi inbox'ta atlanır, tek cevap ve tek ayırma olur")
    void givenSameEventTwice_whenReserveRequested_thenProcessedOnce() {
        // Given
        final OrderflowEvent<StockReserveRequestedPayload> event = new StockReserveRequestedEventBuilder().withValidValues().build();
        final OrderId orderId = new OrderId(event.payload().orderId());

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, event);
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, event);

        // Then
        Awaitility.await("duplicate skipped").atMost(TIMEOUT).until(() -> logTracker.findMessage(Level.INFO,
                "Duplicate message skipped consumer=" + StockReserveRequestedKafkaListener.CONSUMER + " eventId=" + event.metadata().eventId()).isPresent());
        OutboxAssert.of(jdbcTemplate).hasEvents(orderId.value().toString(), EventDefinitions.STOCK_RESERVED.eventType(), 1);
        assertThat(reservedOf()).isEqualTo(ProductValidTestData.Order.QUANTITY.value());
    }

    @Test
    @DisplayName("Aynı sipariş için yeni eventId'li komut (orchestrator'ın yeniden gönderimi): durum değişmez, cevap yeniden yayınlanır")
    void givenRepeatedIntent_whenReserveRequestedAgain_thenReplyRepublishedWithoutReservingAgain() {
        // Given
        final OrderflowEvent<StockReserveRequestedPayload> first = new StockReserveRequestedEventBuilder().withValidValues().build();
        final OrderId orderId = new OrderId(first.payload().orderId());
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, first);
        awaitReservation(orderId);

        final OrderflowEvent<StockReserveRequestedPayload> again = new StockReserveRequestedEventBuilder().withValidValues()
                .withOrderId(orderId.value())
                .build();

        // When
        messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, again);

        // Then
        Awaitility.await("second command processed").atMost(TIMEOUT).untilAsserted(() ->
                InboxAssert.of(jdbcTemplate).processedOnce(StockReserveRequestedKafkaListener.CONSUMER, again.metadata().eventId()));
        OutboxAssert.of(jdbcTemplate).hasEvents(orderId.value().toString(), EventDefinitions.STOCK_RESERVED.eventType(), 2);
        assertThat(reservedOf()).isEqualTo(ProductValidTestData.Order.QUANTITY.value());
        assertThat(logTracker.findMessage(Level.INFO, "Stock reserve request repeated, reply published again orderId=" + orderId.value())).isPresent();
    }

    @Test
    @DisplayName("Satırları olmayan komut retry'sız DLT'ye gider; rezervasyon, cevap ve inbox kaydı yazılmaz")
    void givenCommandWithoutLines_whenConsumed_thenDeadLetteredAndNothingWritten() {
        // Given
        final OrderflowEvent<StockReserveRequestedPayload> event = new StockReserveRequestedEventBuilder().withValidValues().withLines(null).build();
        final String orderId = event.payload().orderId().toString();
        final String deadLetterTopic = TopicNames.deadLetter(TopicNames.STOCK_RESERVE_REQUESTED, "product-service");

        try (MessageProbe probe = MessageProbe.on(KAFKA.getBootstrapServers(), deadLetterTopic)) {

            // When
            messagePublisher.publishEvent(TopicNames.STOCK_RESERVE_REQUESTED, event);

            // Then
            final ConsumerRecord<String, String> deadLetter = probe.awaitMessage(orderId);
            assertThat(MessageProbe.header(deadLetter, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
                    .isEqualTo("com.deveyk.orderflow.shared.messaging.exception.InvalidEventException");

            InboxAssert.of(jdbcTemplate).notProcessed(StockReserveRequestedKafkaListener.CONSUMER, event.metadata().eventId());
            OutboxAssert.of(jdbcTemplate).hasNoEvent(orderId);
            assertThat(stockReservationReadPort.findById(new OrderId(event.payload().orderId()))).isEmpty();
        }
    }

    private StockReservation awaitReservation(final OrderId orderId) {
        return Awaitility.await("reservation %s".formatted(orderId.value()))
                .atMost(TIMEOUT)
                .until(() -> stockReservationReadPort.findById(orderId), found -> found.isPresent())
                .orElseThrow();
    }

    private int reservedOf() {
        return productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow().getReserved().value();
    }
}
