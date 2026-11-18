package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;

import java.util.UUID;

public record OrderLineId(UUID value) {

    public OrderLineId {
        if (value == null) {
            throw new DomainValidationException(SharedErrorCode.ID_REQUIRED, "OrderLineId");
        }
    }

    public static OrderLineId newId() {
        return new OrderLineId(UuidV7.next());
    }
}
