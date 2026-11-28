package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinition;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

// Üretici tarafı: stok cevapları golden JSON sözleşmeleriyle birebir aynı (STRICT). Tüketici tarafı orchestrator'da (adım 5).
class StockSagaEventContractTest {

    private static final OrderId ORDER_ID = new OrderId(UUID.fromString("0198a1b2-0000-7000-8000-00000000a001"));
    private static final Instant OCCURRED_AT = Instant.parse("2026-11-27T10:00:00Z");
    private static final Instant RELEASED_AT = Instant.parse("2026-11-27T10:05:00Z");

    @Test
    @DisplayName("stock.reserved zarfı golden JSON sözleşmesiyle (contracts/stock.reserved.v1.json) birebir aynıdır")
    void givenReservation_whenSerializeStockReserved_thenMatchesGoldenContract() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(ORDER_ID)
                .withCreatedAt(OCCURRED_AT).withLastModifiedAt(OCCURRED_AT).build();

        // When
        final String json = serialize(EventDefinitions.STOCK_RESERVED, "0198a1b2-0000-7000-8000-00000000e201",
                StockReservationToPayloadMapper.toReservedPayload(reservation));

        // Then
        GoldenJson.assertMatches("stock.reserved.v1", json);
    }

    @Test
    @DisplayName("stock.reserve.failed zarfı golden JSON sözleşmesiyle (contracts/stock.reserve.failed.v1.json) birebir aynıdır")
    void givenRejection_whenSerializeStockReserveFailed_thenMatchesGoldenContract() {
        // Given
        final StockRejection rejection = new StockRejection(RejectionReason.INSUFFICIENT_STOCK, ProductValidTestData.Product.PRODUCT_ID);

        // When
        final String json = serialize(EventDefinitions.STOCK_RESERVE_FAILED, "0198a1b2-0000-7000-8000-00000000e202",
                StockReservationToPayloadMapper.toReserveFailedPayload(ORDER_ID, rejection));

        // Then
        GoldenJson.assertMatches("stock.reserve.failed.v1", json);
    }

    @Test
    @DisplayName("stock.released zarfı golden JSON sözleşmesiyle (contracts/stock.released.v1.json) birebir aynıdır")
    void givenReleasedReservation_whenSerializeStockReleased_thenMatchesGoldenContract() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(ORDER_ID)
                .withStatus(ReservationStatus.RELEASED).withCreatedAt(OCCURRED_AT).withLastModifiedAt(RELEASED_AT).build();

        // When
        final String json = serialize(EventDefinitions.STOCK_RELEASED, "0198a1b2-0000-7000-8000-00000000e203",
                StockReservationToPayloadMapper.toReleasedPayload(reservation));

        // Then
        GoldenJson.assertMatches("stock.released.v1", json);
    }

    private static String serialize(final EventDefinition definition, final String eventId, final Object payload) {
        final EventMetadata metadata = new EventMetadata(UUID.fromString(eventId), definition.eventType(), definition.aggregateType(),
                ORDER_ID.value().toString(), EventSourceNames.PRODUCT_SERVICE, "1", OCCURRED_AT,
                "4bf92f3577b34da6a3ce929d0e0e4736", "black-friday-checkout-1");
        return JsonMapper.builder().build().writeValueAsString(new OrderflowEvent<>(metadata, payload));
    }
}
