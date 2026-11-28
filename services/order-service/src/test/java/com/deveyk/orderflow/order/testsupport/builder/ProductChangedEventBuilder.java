package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.messaging.AggregateTypeNames;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.UUID;

// product-service'in (Faz 3) yayınlayacağı product.changed zarfı. eventId ve productId her builder'da yenidir:
// Kafka testler arasında temizlenmez, seed'deki ürünler de değişmemeli (07, karar 13).
public class ProductChangedEventBuilder implements TestDataBuilder<OrderflowEvent<ProductChangedPayload>> {

    private UUID eventId;
    private UUID productId;
    private String name;
    private MoneyAmount unitPrice;
    private Long productVersion;

    public ProductChangedEventBuilder withValidValues() {
        this.eventId = UUID.randomUUID();
        this.productId = UUID.randomUUID();
        this.name = OrderValidTestData.Product.NAME;
        this.unitPrice = MoneyAmount.of(OrderValidTestData.Product.UNIT_PRICE, OrderValidTestData.TRY);
        this.productVersion = 1L;
        return this;
    }

    public ProductChangedEventBuilder withEventId(final UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    public ProductChangedEventBuilder withProductId(final UUID productId) {
        this.productId = productId;
        return this;
    }

    public ProductChangedEventBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ProductChangedEventBuilder withUnitPrice(final MoneyAmount unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public ProductChangedEventBuilder withProductVersion(final Long productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    @Override
    public OrderflowEvent<ProductChangedPayload> build() {
        final EventMetadata metadata = new EventMetadata(eventId, EventTypeNames.PRODUCT_CHANGED,
                AggregateTypeNames.PRODUCT, String.valueOf(productId), EventSourceNames.PRODUCT_SERVICE, "1",
                OrderValidTestData.NOW, null, null);

        final ProductChangedPayload payload = ProductChangedPayload.builder()
                .productId(productId)
                .name(name)
                .unitPrice(unitPrice)
                .productVersion(productVersion)
                .build();

        return new OrderflowEvent<>(metadata, payload);
    }
}
