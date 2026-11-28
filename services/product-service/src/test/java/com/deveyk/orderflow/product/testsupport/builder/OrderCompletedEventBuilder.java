package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.math.BigDecimal;
import java.util.UUID;

// order-service'in (adım 6) yayınlayacağı order.completed zarfı.
public class OrderCompletedEventBuilder implements TestDataBuilder<OrderflowEvent<OrderCompletedPayload>> {

    private UUID eventId;
    private UUID orderId;

    public OrderCompletedEventBuilder withValidValues() {
        this.eventId = UUID.randomUUID();
        this.orderId = UUID.randomUUID();
        return this;
    }

    public OrderCompletedEventBuilder withOrderId(final UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    @Override
    public OrderflowEvent<OrderCompletedPayload> build() {
        final EventMetadata metadata = new EventMetadata(eventId, EventDefinitions.ORDER_COMPLETED.eventType(),
                EventDefinitions.ORDER_COMPLETED.aggregateType(), String.valueOf(orderId), EventSourceNames.ORDER_SERVICE, "1",
                ProductValidTestData.LATER, null, null);

        return new OrderflowEvent<>(metadata, OrderCompletedPayload.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .totalAmount(MoneyAmount.of(new BigDecimal("499.80"), ProductValidTestData.TRY))
                .completedAt(ProductValidTestData.LATER)
                .build());
    }
}
