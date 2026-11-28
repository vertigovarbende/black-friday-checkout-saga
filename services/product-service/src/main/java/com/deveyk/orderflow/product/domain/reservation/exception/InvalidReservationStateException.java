package com.deveyk.orderflow.product.domain.reservation.exception;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;

import java.io.Serial;

// Hedef duruma artık gidilemiyor (COMMITTED rezervasyonu release etmek gibi): retry yok, DLT (checkout-saga.md §5).
public final class InvalidReservationStateException extends OrderflowInvalidStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidReservationStateException(final OrderId orderId, final ReservationStatus current, final ReservationStatus target) {
        super(StockReservationErrorCode.INVALID_RESERVATION_STATE, orderId.value().toString(), current.name(), target.name());
    }
}
