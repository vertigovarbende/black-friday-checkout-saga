package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;

import java.util.UUID;

// Siparişin kimliği; burada üretilmez, saga mesajıyla gelir. Rezervasyon siparişle 1:1 olduğu için onun kimliğidir
// (event-catalog.md §1).
public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new DomainValidationException(SharedErrorCode.ID_REQUIRED, "OrderId");
        }
    }
}
