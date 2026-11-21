package com.deveyk.orderflow.order.adapter.out.messaging;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import com.deveyk.orderflow.shared.messaging.AggregateTypeNames;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;

class OrderCreatedContractTest {

    private static final OrderId ORDER_ID = new OrderId(UUID.fromString("0198a1b2-0000-7000-8000-00000000a001"));
    private static final UUID EVENT_ID = UUID.fromString("0198a1b2-0000-7000-8000-00000000e001");

    @Test
    @DisplayName("order.created zarfı golden JSON sözleşmesiyle (contracts/order.created.v1.json) birebir aynıdır")
    void givenPlacedOrder_whenSerializeOrderCreated_thenMatchesGoldenContract() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withId(ORDER_ID)
                .withLines(List.of(
                        new OrderLineBuilder()
                                .withValidValues()
                                .withQuantity(new Quantity(2))
                                .build(),
                        new OrderLineBuilder()
                                .withValidValues()
                                .withProductId(new ProductId(OrderValidTestData.SecondProduct.ID))
                                .withUnitPrice(new Money(OrderValidTestData.SecondProduct.UNIT_PRICE, TRY))
                                .build()))
                .build();

        final EventMetadata metadata = new EventMetadata(EVENT_ID, EventTypeNames.ORDER_CREATED,
                AggregateTypeNames.ORDER, ORDER_ID.value().toString(), EventSourceNames.ORDER_SERVICE, "1",
                OrderValidTestData.NOW, "4bf92f3577b34da6a3ce929d0e0e4736", "black-friday-checkout-1");

        // When
        final String json = JsonMapper.builder().build()
                .writeValueAsString(new OrderflowEvent<>(metadata, OrderToOrderCreatedPayloadMapper.toPayload(order)));

        // Then
        GoldenJson.assertMatches("order.created.v1", json);
    }
}
