package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;

import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new DomainValidationException(SharedErrorCode.ID_REQUIRED, "OrderId");
        }
    }

    public static OrderId newId() {
        return new OrderId(UuidV7.next());
    }
}
