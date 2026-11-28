package com.deveyk.orderflow.product.application.product.usecase;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.exception.ProductErrorCode;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// PATCH: null alan değişmez.
public record ChangeProductDetailsUseCase(ProductId productId, String name, Money unitPrice) implements UseCase {

    public ChangeProductDetailsUseCase {

        if (productId == null) {
            throw new DomainValidationException(ProductErrorCode.PRODUCT_ID_REQUIRED);
        }

    }
}
