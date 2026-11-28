package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.ReleaseStockUseCase;
import com.deveyk.orderflow.product.application.reservation.usecase.ReserveStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RequestedLine;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventJsonReader;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleaseRequestedPayload;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

// Tüketici tarafı: orchestrator'ın stok komutlarını okuyabiliyor muyuz? Üretici tarafı (assertMatches) orchestrator'da, adım 5.
class StockCommandContractTest {

    private static final OrderId ORDER_ID = new OrderId(UUID.fromString("0198a1b2-0000-7000-8000-00000000a001"));
    private static final EventJsonReader EVENT_JSON_READER = new EventJsonReader(JsonMapper.builder().build());

    @Test
    @DisplayName("stock.reserve.requested golden JSON sözleşmesi (contracts/stock.reserve.requested.v1.json) okunur ve use case'e çevrilir")
    void givenGoldenReserveRequested_whenReadAndMap_thenUseCaseMatches() {
        // Given
        final String json = GoldenJson.read("stock.reserve.requested.v1");

        // When
        final OrderflowEvent<StockReserveRequestedPayload> event = EVENT_JSON_READER.read(json, StockReserveRequestedPayload.class);
        final ReserveStockUseCase useCase = StockReserveRequestedPayloadToUseCaseMapper.toUseCase(event.payload());

        // Then
        assertThat(event.metadata().eventType()).isEqualTo(EventDefinitions.STOCK_RESERVE_REQUESTED.eventType());
        assertThat(event.metadata().aggregateType()).isEqualTo(EventDefinitions.STOCK_RESERVE_REQUESTED.aggregateType());
        assertThat(event.metadata().source()).isEqualTo(EventSourceNames.ORCHESTRATOR_SERVICE);
        assertThat(event.metadata().aggregateId()).isEqualTo(ORDER_ID.value().toString());

        assertThat(useCase.orderId()).isEqualTo(ORDER_ID);
        assertThat(useCase.lines()).containsExactly(
                new RequestedLine(ProductValidTestData.Product.PRODUCT_ID, new Quantity(2), new Money(new BigDecimal("249.90"), TRY)),
                new RequestedLine(ProductValidTestData.SecondProduct.PRODUCT_ID, new Quantity(1), new Money(new BigDecimal("15.00"), TRY)));
    }

    @Test
    @DisplayName("stock.release.requested golden JSON sözleşmesi (contracts/stock.release.requested.v1.json) okunur ve use case'e çevrilir")
    void givenGoldenReleaseRequested_whenReadAndMap_thenUseCaseMatches() {
        // Given
        final String json = GoldenJson.read("stock.release.requested.v1");

        // When
        final OrderflowEvent<StockReleaseRequestedPayload> event = EVENT_JSON_READER.read(json, StockReleaseRequestedPayload.class);
        final ReleaseStockUseCase useCase = StockReleaseRequestedPayloadToUseCaseMapper.toUseCase(event.payload());

        // Then
        assertThat(event.metadata().eventType()).isEqualTo(EventDefinitions.STOCK_RELEASE_REQUESTED.eventType());
        assertThat(event.metadata().aggregateType()).isEqualTo(EventDefinitions.STOCK_RELEASE_REQUESTED.aggregateType());
        assertThat(event.metadata().source()).isEqualTo(EventSourceNames.ORCHESTRATOR_SERVICE);

        assertThat(useCase.orderId()).isEqualTo(ORDER_ID);
    }
}
