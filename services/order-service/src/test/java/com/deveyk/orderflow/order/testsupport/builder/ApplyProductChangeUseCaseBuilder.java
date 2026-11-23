package com.deveyk.orderflow.order.testsupport.builder;

import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.testsupport.TestDataBuilder;

// withValidValues() ProductChangedEventBuilder ile aynı değerleri verir: o event'in use case'e çevrilmiş hali.
public class ApplyProductChangeUseCaseBuilder implements TestDataBuilder<ApplyProductChangeUseCase> {

    private ProductId productId;
    private String name;
    private Money unitPrice;
    private long productVersion;

    public ApplyProductChangeUseCaseBuilder withValidValues() {
        this.productId = OrderValidTestData.Product.PRODUCT_ID;
        this.name = OrderValidTestData.Product.NAME;
        this.unitPrice = OrderValidTestData.Product.UNIT_PRICE_MONEY;
        this.productVersion = 1L;
        return this;
    }

    public ApplyProductChangeUseCaseBuilder withProductId(final ProductId productId) {
        this.productId = productId;
        return this;
    }

    public ApplyProductChangeUseCaseBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ApplyProductChangeUseCaseBuilder withUnitPrice(final Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    public ApplyProductChangeUseCaseBuilder withProductVersion(final long productVersion) {
        this.productVersion = productVersion;
        return this;
    }

    @Override
    public ApplyProductChangeUseCase build() {
        return new ApplyProductChangeUseCase(this.productId, this.name, this.unitPrice, this.productVersion);
    }
}
