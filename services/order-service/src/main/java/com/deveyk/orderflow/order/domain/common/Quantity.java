package com.deveyk.orderflow.order.domain.common;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

public record Quantity(int value) {

    public Quantity {
        if (value < 0) {
            throw new DomainValidationException(CommonDomainErrorCode.QUANTITY_NEGATIVE, String.valueOf(value));
        }
    }

    public boolean isPositive() {
        return value > 0;
    }
}
