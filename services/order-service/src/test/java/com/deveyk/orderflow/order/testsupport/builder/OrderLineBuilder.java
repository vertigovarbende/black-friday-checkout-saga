package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderLineId;
import com.deveyk.orderflow.order.domain.order.model.ProductId;
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
        this.productId = new ProductId(OrderValidTestData.Product.ID);
        this.productName = OrderValidTestData.Product.NAME;
        this.unitPrice = new Money(OrderValidTestData.Product.UNIT_PRICE, OrderValidTestData.TRY);
        this.quantity = new Quantity(1);
        return this;
    }

    public OrderLineBuilder withRandomProduct() {
        this.productId = new ProductId(UUID.randomUUID());
        return this;
    }

    public OrderLineBuilder withProductId(final ProductId productId) {
        this.productId = productId;
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
