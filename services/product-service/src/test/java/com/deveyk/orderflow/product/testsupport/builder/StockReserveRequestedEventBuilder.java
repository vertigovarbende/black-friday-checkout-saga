package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// orchestrator'ın (adım 5) yayınlayacağı stock.reserve.requested zarfı. eventId ve orderId her builder'da yenidir:
// Kafka testler arasında temizlenmez (07, karar 13).
public class StockReserveRequestedEventBuilder implements TestDataBuilder<OrderflowEvent<StockReserveRequestedPayload>> {

    private UUID eventId;
    private UUID orderId;
    private List<StockReserveRequestedPayload.Line> lines;

    // Tek satır: ProductValidTestData.Product'tan 2 adet, güncel fiyatla
    public StockReserveRequestedEventBuilder withValidValues() {
        this.eventId = UUID.randomUUID();
        this.orderId = UUID.randomUUID();
        this.lines = new ArrayList<>(List.of(line(ProductValidTestData.Product.ID, ProductValidTestData.Order.QUANTITY.value(),
                MoneyAmount.of(ProductValidTestData.Product.UNIT_PRICE, ProductValidTestData.TRY))));
        return this;
    }

    public StockReserveRequestedEventBuilder withEventId(final UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    public StockReserveRequestedEventBuilder withOrderId(final UUID orderId) {
        this.orderId = orderId;
        return this;
    }

    public StockReserveRequestedEventBuilder withLines(final List<StockReserveRequestedPayload.Line> lines) {
        this.lines = lines;
        return this;
    }

    public static StockReserveRequestedPayload.Line line(final UUID productId, final int quantity, final MoneyAmount unitPrice) {
        return StockReserveRequestedPayload.Line.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    @Override
    public OrderflowEvent<StockReserveRequestedPayload> build() {
        final EventMetadata metadata = new EventMetadata(eventId, EventDefinitions.STOCK_RESERVE_REQUESTED.eventType(),
                EventDefinitions.STOCK_RESERVE_REQUESTED.aggregateType(), String.valueOf(orderId), EventSourceNames.ORCHESTRATOR_SERVICE, "1",
                ProductValidTestData.NOW, null, null);

        return new OrderflowEvent<>(metadata, StockReserveRequestedPayload.builder()
                .orderId(orderId)
                .lines(lines)
                .build());
    }
}
