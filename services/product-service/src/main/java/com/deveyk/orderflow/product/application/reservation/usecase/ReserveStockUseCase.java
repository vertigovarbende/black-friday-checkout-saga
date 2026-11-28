package com.deveyk.orderflow.product.application.reservation.usecase;

import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RequestedLine;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

import java.util.List;

// stock.reserve.requested (saga T2). Satırların kuralları domain'de (StockReservation.request).
public record ReserveStockUseCase(OrderId orderId, List<RequestedLine> lines) implements UseCase {

    public ReserveStockUseCase {

        if (orderId == null) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_ID_REQUIRED);
        }

    }
}
