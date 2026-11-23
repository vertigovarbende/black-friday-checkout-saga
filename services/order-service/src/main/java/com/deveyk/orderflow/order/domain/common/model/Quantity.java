package com.deveyk.orderflow.order.domain.common.model;

import com.deveyk.orderflow.order.domain.common.exception.CommonDomainErrorCode;
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
