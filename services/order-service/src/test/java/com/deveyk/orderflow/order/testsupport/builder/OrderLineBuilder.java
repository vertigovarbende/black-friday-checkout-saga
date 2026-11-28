package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderLineId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.UUID;

public class OrderLineBuilder implements TestDataBuilder<OrderLine> {

    private OrderLineId id;
    private ProductId productId;
    private String productName;
    private Money unitPrice;
    private Quantity quantity;

    public OrderLineBuilder withValidValues() {
        this.id = OrderLineId.newId();
        this.productId = OrderValidTestData.Product.PRODUCT_ID;
        this.productName = OrderValidTestData.Product.NAME;
        this.unitPrice = OrderValidTestData.Product.UNIT_PRICE_MONEY;
        this.quantity = new Quantity(1);
        return this;
    }

    // Seed'deki ikinci ürün (USB-C Cable): kimliği, adı ve fiyatı birlikte
    public OrderLineBuilder withSecondProduct() {
        this.productId = OrderValidTestData.SecondProduct.PRODUCT_ID;
        this.productName = OrderValidTestData.SecondProduct.NAME;
        this.unitPrice = OrderValidTestData.SecondProduct.UNIT_PRICE_MONEY;
        return this;
    }

    public OrderLineBuilder withRandomProduct() {
        this.productId = new ProductId(UUID.randomUUID());
        return this;
    }

    public OrderLineBuilder withId(final OrderLineId id) {
        this.id = id;
        return this;
    }

    public OrderLineBuilder withProductId(final ProductId productId) {
        this.productId = productId;
        return this;
    }

    public OrderLineBuilder withProductName(final String productName) {
        this.productName = productName;
        return this;
    }

    public OrderLineBuilder withUnitPrice(final Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public OrderLineBuilder withQuantity(final Quantity quantity) {
        this.quantity = quantity;
        return this;
    }

    @Override
    public OrderLine build() {
        return OrderLine.rehydrate()
                .id(this.id)
                .productId(this.productId)
                .productName(this.productName)
                .unitPrice(this.unitPrice)
                .quantity(this.quantity)
                .build();
    }
}
