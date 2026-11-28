package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleaseRequestedPayload;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.UUID;

public class StockReleaseRequestedEventBuilder implements TestDataBuilder<OrderflowEvent<StockReleaseRequestedPayload>> {

    private UUID eventId;
    private UUID orderId;

    public StockReleaseRequestedEventBuilder withValidValues() {
        this.eventId = UUID.randomUUID();
        this.orderId = UUID.randomUUID();
        return this;
    }

    public StockReleaseRequestedEventBuilder withOrderId(final UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    @Override
    public OrderflowEvent<StockReleaseRequestedPayload> build() {
        final EventMetadata metadata = new EventMetadata(eventId, EventDefinitions.STOCK_RELEASE_REQUESTED.eventType(),
                EventDefinitions.STOCK_RELEASE_REQUESTED.aggregateType(), String.valueOf(orderId), EventSourceNames.ORCHESTRATOR_SERVICE, "1",
                ProductValidTestData.LATER, null, null);

        return new OrderflowEvent<>(metadata, new StockReleaseRequestedPayload(orderId));
    }
}
