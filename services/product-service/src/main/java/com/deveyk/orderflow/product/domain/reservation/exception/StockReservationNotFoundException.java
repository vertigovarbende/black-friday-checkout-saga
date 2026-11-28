package com.deveyk.orderflow.product.domain.reservation.exception;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;

import java.io.Serial;

public final class StockReservationNotFoundException extends OrderflowResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public StockReservationNotFoundException(final OrderId orderId) {
        super(StockReservationErrorCode.RESERVATION_NOT_FOUND, orderId.value().toString());
    }
}
