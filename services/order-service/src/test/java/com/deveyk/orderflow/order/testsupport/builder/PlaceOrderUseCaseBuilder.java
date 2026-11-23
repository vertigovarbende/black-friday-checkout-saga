package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlaceOrderUseCaseBuilder implements TestDataBuilder<PlaceOrderUseCase> {

    private CustomerId customerId;
    private List<PlaceOrderUseCase.Line> lines = new ArrayList<>();

    public PlaceOrderUseCaseBuilder withValidValues() {
        this.customerId = OrderValidTestData.Customer.CUSTOMER_ID;
        this.lines = new ArrayList<>(List.of(new PlaceOrderUseCase.Line(OrderValidTestData.Product.PRODUCT_ID, new Quantity(1))));
        return this;
    }

    public PlaceOrderUseCaseBuilder withCustomerId(final CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    // Satır ekler: new PlaceOrderUseCaseBuilder().withCustomerId(c).withLine(a, 2).withLine(b, 1)
    public PlaceOrderUseCaseBuilder withLine(final ProductId productId, final int quantity) {
        this.lines.add(new PlaceOrderUseCase.Line(productId, new Quantity(quantity)));
        return this;
    }

    @Override
    public PlaceOrderUseCase build() {
        return new PlaceOrderUseCase(this.customerId, this.lines);
    }
}
