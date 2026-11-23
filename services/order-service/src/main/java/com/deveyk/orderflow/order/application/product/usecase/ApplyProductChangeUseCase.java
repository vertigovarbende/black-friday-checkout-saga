package com.deveyk.orderflow.order.application.product.usecase;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

public record ApplyProductChangeUseCase(ProductId productId, String name, Money unitPrice, long productVersion) implements UseCase {

    public ApplyProductChangeUseCase {

        if (productId == null) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_ID_REQUIRED);
        }

    }
}
