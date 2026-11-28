package com.deveyk.orderflow.product.domain.common.model;

import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// Adet ya da stok sayacı; sıfır olabilir, negatif olamaz. minus'un sonucu negatifse oluşturulamaz.
public record Quantity(int value) {

    public static final Quantity ZERO = new Quantity(0);

    public Quantity {
        if (value < 0) {
            throw new DomainValidationException(CommonDomainErrorCode.QUANTITY_NEGATIVE, String.valueOf(value));
        }
    }

    public boolean isPositive() {
        return value > 0;
    }

    public boolean isAtLeast(final Quantity other) {
        return value >= other.value;
    }

    public Quantity plus(final Quantity other) {
        return new Quantity(value + other.value);
    }

    public Quantity minus(final Quantity other) {
        return new Quantity(value - other.value);
    }
}
