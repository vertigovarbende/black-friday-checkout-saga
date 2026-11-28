package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

public record ReservationLine(ProductId productId, Quantity quantity) {

    public ReservationLine {

        if (productId == null) {
            throw new DomainValidationException(StockReservationErrorCode.LINE_PRODUCT_REQUIRED);
        }

        if (quantity == null || !quantity.isPositive()) {
            throw new DomainValidationException(StockReservationErrorCode.LINE_QUANTITY_MUST_BE_POSITIVE);
        }
    }
}
