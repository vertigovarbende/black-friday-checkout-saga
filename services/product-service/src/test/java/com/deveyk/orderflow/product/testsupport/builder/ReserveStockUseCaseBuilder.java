package com.deveyk.orderflow.product.testsupport.builder;

import com.deveyk.orderflow.product.application.reservation.usecase.ReserveStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RequestedLine;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;

public class ReserveStockUseCaseBuilder implements TestDataBuilder<ReserveStockUseCase> {

    private OrderId orderId;
    private List<RequestedLine> lines = new ArrayList<>();

    // Tek satır: ProductValidTestData.Product'tan güncel fiyatla 2 adet
    public ReserveStockUseCaseBuilder withValidValues() {
        this.orderId = ProductValidTestData.Order.ORDER_ID;
        this.lines = new ArrayList<>(List.of(new RequestedLine(ProductValidTestData.Product.PRODUCT_ID,
                ProductValidTestData.Order.QUANTITY, ProductValidTestData.Product.UNIT_PRICE_MONEY)));
        return this;
    }

    public ReserveStockUseCaseBuilder withOrderId(final OrderId orderId) {
        this.orderId = orderId;
        return this;
    }

    public ReserveStockUseCaseBuilder withLine(final ProductId productId, final int quantity, final Money unitPrice) {
        this.lines.add(new RequestedLine(productId, new Quantity(quantity), unitPrice));
        return this;
    }

    public ReserveStockUseCaseBuilder withLines(final List<RequestedLine> lines) {
        this.lines = lines == null ? null : new ArrayList<>(lines);
        return this;
    }

    @Override
    public ReserveStockUseCase build() {
        return new ReserveStockUseCase(this.orderId, this.lines);
    }
}
