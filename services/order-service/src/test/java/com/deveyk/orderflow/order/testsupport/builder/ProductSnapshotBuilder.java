package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.UUID;

public class ProductSnapshotBuilder implements TestDataBuilder<ProductSnapshot> {

    private ProductId id;
    private String name;
    private Money unitPrice;
    private long productVersion;
    private long version;

    public ProductSnapshotBuilder withValidValues() {
        this.id = OrderValidTestData.Product.PRODUCT_ID;
        this.name = OrderValidTestData.Product.NAME;
        this.unitPrice = OrderValidTestData.Product.UNIT_PRICE_MONEY;
        this.productVersion = 0L;
        this.version = 0L;
        return this;
    }

    // Seed'deki ikinci ürün (USB-C Cable): kimliği, adı ve fiyatı birlikte
    public ProductSnapshotBuilder withSecondProduct() {
        this.id = OrderValidTestData.SecondProduct.PRODUCT_ID;
        this.name = OrderValidTestData.SecondProduct.NAME;
        this.unitPrice = OrderValidTestData.SecondProduct.UNIT_PRICE_MONEY;
        return this;
    }

    // Seed'de olmayan, her çağrıda yeni bir ürün; DB testlerinde seed'deki kopyalar değişmesin diye (07, karar 13)
    public ProductSnapshotBuilder withRandomProduct() {
        this.id = new ProductId(UUID.randomUUID());
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

    public ProductSnapshotBuilder withVersion(final long version) {
        this.version = version;
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
