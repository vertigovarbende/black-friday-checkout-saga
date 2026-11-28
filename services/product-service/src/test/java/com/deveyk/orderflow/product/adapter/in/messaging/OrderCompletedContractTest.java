package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.CommitStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventJsonReader;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Tüketici tarafı. Üretici tarafı (assertMatches) order-service'te, adım 6.
class OrderCompletedContractTest {

    @Test
    @DisplayName("order.completed golden JSON sözleşmesi (contracts/order.completed.v1.json) okunur ve use case'e çevrilir")
    void givenGoldenContract_whenReadAndMap_thenUseCaseMatches() {
        // Given
        final String json = GoldenJson.read("order.completed.v1");

        // When
        final OrderflowEvent<OrderCompletedPayload> event = new EventJsonReader(JsonMapper.builder().build())
                .read(json, OrderCompletedPayload.class);
        final CommitStockUseCase useCase = OrderCompletedPayloadToUseCaseMapper.toUseCase(event.payload());

        // Then
        assertThat(event.metadata().eventType()).isEqualTo(EventDefinitions.ORDER_COMPLETED.eventType());
        assertThat(event.metadata().aggregateType()).isEqualTo(EventDefinitions.ORDER_COMPLETED.aggregateType());
        assertThat(event.metadata().source()).isEqualTo(EventSourceNames.ORDER_SERVICE);

        assertThat(useCase.orderId()).isEqualTo(new OrderId(UUID.fromString("0198a1b2-0000-7000-8000-00000000a001")));
    }
}
