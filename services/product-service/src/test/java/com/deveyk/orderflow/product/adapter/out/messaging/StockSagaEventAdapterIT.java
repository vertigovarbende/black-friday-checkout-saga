package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockSagaEventAdapterIT extends ProductTestContainerConfiguration {

    @Autowired
    private StockSagaEventAdapter stockSagaEventAdapter;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("stock.reserved outbox'a yazılır: topic, tip, aggregate = stock / orderId")
    void givenReservation_whenPublishStockReserved_thenOutboxRow() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(new OrderId(UUID.randomUUID())).build();

        // When
        transactionTemplate.executeWithoutResult(status -> stockSagaEventAdapter.publishStockReserved(reservation));

        // Then
        final OutboxAssert.OutboxRow row = OutboxAssert.of(jdbcTemplate).hasEvent(reservation.getId().value().toString(),
                EventDefinitions.STOCK_RESERVED.eventType());
        assertThat(row.topic()).isEqualTo(TopicNames.STOCK_RESERVED);
        assertThat(row.aggregateType()).isEqualTo(EventDefinitions.STOCK_RESERVED.aggregateType());
    }

    @Test
    @DisplayName("Kapanmış rezervasyona gelen reserve'ün cevabı: stock.reserve.failed, RESERVATION_CLOSED, productId null")
    void givenReservationClosed_whenPublishStockReserveFailed_thenOutboxRowWithoutProduct() {
        // Given
        final OrderId orderId = new OrderId(UUID.randomUUID());

        // When
        transactionTemplate.executeWithoutResult(status -> stockSagaEventAdapter.publishStockReserveFailed(orderId, StockRejection.reservationClosed()));

        // Then
        final OutboxAssert.OutboxRow row = OutboxAssert.of(jdbcTemplate).hasEvent(orderId.value().toString(),
                EventDefinitions.STOCK_RESERVE_FAILED.eventType());
        assertThat(row.topic()).isEqualTo(TopicNames.STOCK_RESERVE_FAILED);
        assertThat(JsonPath.<String>read(row.payload(), "$.payload.reason")).isEqualTo("RESERVATION_CLOSED");
        assertThat(JsonPath.<Object>read(row.payload(), "$.payload.productId")).isNull();
    }

    @Test
    @DisplayName("stock.released outbox'a yazılır")
    void givenReleasedReservation_whenPublishStockReleased_thenOutboxRow() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(new OrderId(UUID.randomUUID())).withClosingRecord().build();

        // When
        transactionTemplate.executeWithoutResult(status -> stockSagaEventAdapter.publishStockReleased(reservation));

        // Then
        assertThat(OutboxAssert.of(jdbcTemplate).hasEvent(reservation.getId().value().toString(),
                EventDefinitions.STOCK_RELEASED.eventType()).topic()).isEqualTo(TopicNames.STOCK_RELEASED);
    }
}
