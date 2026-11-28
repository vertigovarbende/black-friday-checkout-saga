package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

public class ProductBuilder implements TestDataBuilder<Product> {

    private ProductId id;
    private String name;
    private Money unitPrice;
    private Quantity onHand;
    private Quantity reserved;
    private long catalogVersion;
    private long version;

    public ProductBuilder withValidValues() {
        this.id = ProductValidTestData.Product.PRODUCT_ID;
        this.name = ProductValidTestData.Product.NAME;
        this.unitPrice = ProductValidTestData.Product.UNIT_PRICE_MONEY;
        this.onHand = ProductValidTestData.Product.ON_HAND;
        this.reserved = Quantity.ZERO;
        this.catalogVersion = 1L;
        this.version = 0L;
        return this;
    }

    // İkinci ürün (USB-C Cable): kimliği, adı, fiyatı ve stoğu birlikte
    public ProductBuilder withSecondProduct() {
        this.id = ProductValidTestData.SecondProduct.PRODUCT_ID;
        this.name = ProductValidTestData.SecondProduct.NAME;
        this.unitPrice = ProductValidTestData.SecondProduct.UNIT_PRICE_MONEY;
        this.onHand = ProductValidTestData.SecondProduct.ON_HAND;
        return this;
    }

    public ProductBuilder withId(final ProductId id) {
        this.id = id;
        return this;
    }

    public ProductBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder withUnitPrice(final Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public ProductBuilder withOnHand(final int onHand) {
        this.onHand = new Quantity(onHand);
        return this;
    }

    public ProductBuilder withReserved(final int reserved) {
        this.reserved = new Quantity(reserved);
        return this;
    }

    public ProductBuilder withCatalogVersion(final long catalogVersion) {
        this.catalogVersion = catalogVersion;
        return this;
    }

    public ProductBuilder withVersion(final long version) {
        this.version = version;
        return this;
    }

    @Override
    public Product build() {
        return Product.rehydrate()
                .id(this.id)
                .name(this.name)
                .unitPrice(this.unitPrice)
                .onHand(this.onHand)
                .reserved(this.reserved)
                .catalogVersion(this.catalogVersion)
                .version(this.version)
                .build();
    }
}
