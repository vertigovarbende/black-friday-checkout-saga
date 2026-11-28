package com.deveyk.orderflow.product.adapter.in.web.mapper;

import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.request.MoneyRequest;
import com.deveyk.orderflow.product.application.product.usecase.ChangeProductDetailsUseCase;
import com.deveyk.orderflow.product.application.product.usecase.ListProductsUseCase;
import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

import java.util.Currency;
import java.util.UUID;

public final class ProductRequestToUseCaseMapper {

    private ProductRequestToUseCaseMapper() {
    }

    public static ListProductsUseCase toListProductsUseCase() {
        return new ListProductsUseCase();
    }

    public static ChangeProductDetailsUseCase toChangeProductDetailsUseCase(final UUID productId, final ChangeProductDetailsRequest request) {
        return new ChangeProductDetailsUseCase(new ProductId(productId), request.name(), toMoney(request.unitPrice()));
    }

    private static Money toMoney(final MoneyRequest unitPrice) {

        if (unitPrice == null) {
            return null;
        }

        return new Money(unitPrice.amount(), toCurrency(unitPrice.currency()));
    }

    private static Currency toCurrency(final String code) {
        try {
            return Currency.getInstance(code);
        } catch (final IllegalArgumentException e) {
            throw new DomainValidationException(CommonDomainErrorCode.CURRENCY_UNKNOWN, code);
        }
    }
}
