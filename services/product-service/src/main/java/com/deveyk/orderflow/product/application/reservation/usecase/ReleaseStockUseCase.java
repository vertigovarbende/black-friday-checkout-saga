package com.deveyk.orderflow.product.application.reservation.usecase;

import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// stock.release.requested (telafi C2).
public record ReleaseStockUseCase(OrderId orderId) implements UseCase {

    public ReleaseStockUseCase {

        if (orderId == null) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_ID_REQUIRED);
        }

    }
}
