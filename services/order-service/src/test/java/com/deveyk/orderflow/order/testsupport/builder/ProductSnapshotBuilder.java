package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

public class ProductSnapshotBuilder implements TestDataBuilder<ProductSnapshot> {

    private ProductId id;
    private String name;
    private Money unitPrice;
    private long productVersion;
    private long version;

    public ProductSnapshotBuilder withValidValues() {
        this.id = new ProductId(OrderValidTestData.Product.ID);
        this.name = OrderValidTestData.Product.NAME;
        this.unitPrice = new Money(OrderValidTestData.Product.UNIT_PRICE, OrderValidTestData.TRY);
        this.productVersion = 0L;
        this.version = 0L;
        return this;
    }

    public ProductSnapshotBuilder withId(final ProductId id) {
        this.id = id;
        return this;
    }

    public ProductSnapshotBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ProductSnapshotBuilder withUnitPrice(final Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public ProductSnapshotBuilder withProductVersion(final long productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    @Override
    public ProductSnapshot build() {
        return ProductSnapshot.rehydrate()
                .id(this.id)
                .name(this.name)
                .unitPrice(this.unitPrice)
                .productVersion(this.productVersion)
                .version(this.version)
                .build();
    }
}
