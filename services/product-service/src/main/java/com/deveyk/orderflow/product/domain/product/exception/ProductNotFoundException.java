package com.deveyk.orderflow.product.domain.product.exception;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;

import java.io.Serial;

public final class ProductNotFoundException extends OrderflowResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProductNotFoundException(final ProductId productId) {
        super(ProductErrorCode.PRODUCT_NOT_FOUND, productId.value().toString());
    }
}
