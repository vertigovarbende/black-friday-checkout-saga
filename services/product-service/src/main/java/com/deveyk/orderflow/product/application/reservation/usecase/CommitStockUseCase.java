package com.deveyk.orderflow.product.application.reservation.usecase;

import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// order.completed: rezervasyon kesinleşir; saga'ya cevap yoktur (05 §8).
public record CommitStockUseCase(OrderId orderId) implements UseCase {

    public CommitStockUseCase {

        if (orderId == null) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_ID_REQUIRED);
        }

    }
}
