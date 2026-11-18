package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;

import java.util.UUID;

public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new DomainValidationException(SharedErrorCode.ID_REQUIRED, "ProductId");
        }
    }
}
