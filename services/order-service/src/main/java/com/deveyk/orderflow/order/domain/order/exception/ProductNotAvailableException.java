package com.deveyk.orderflow.order.domain.order.exception;

import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

public final class ProductNotAvailableException extends OrderflowBusinessRuleException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProductNotAvailableException(final List<ProductId> productIds) {
        super(OrderErrorCode.PRODUCT_NOT_AVAILABLE, productIds.stream().map(productId -> productId.value().toString()).collect(Collectors.joining(", ")));
    }
}
