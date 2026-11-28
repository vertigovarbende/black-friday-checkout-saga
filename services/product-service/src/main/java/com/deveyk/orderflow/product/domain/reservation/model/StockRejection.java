package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// stock.reserve.failed'in içeriği. productId sebebi olan satırın ürünüdür; sadece RESERVATION_CLOSED'da yoktur (null).
public record StockRejection(RejectionReason reason, ProductId productId) {

    public StockRejection {

        if (reason == null) {
            throw new DomainValidationException(StockReservationErrorCode.REJECTION_REASON_REQUIRED);
        }

        if ((productId == null) != (reason == RejectionReason.RESERVATION_CLOSED)) {
            throw new DomainValidationException(StockReservationErrorCode.REJECTED_PRODUCT_MISMATCH, reason.name());
        }
    }

    public static StockRejection reservationClosed() {
        return new StockRejection(RejectionReason.RESERVATION_CLOSED, null);
    }
}
