package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.CommitStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;

// Stok commit'i için sadece orderId gerekir; customerId ve totalAmount notification'ın.
final class OrderCompletedPayloadToUseCaseMapper {

    private OrderCompletedPayloadToUseCaseMapper() {
    }

    static CommitStockUseCase toUseCase(final OrderCompletedPayload payload) {

        if (payload.orderId() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.orderId");
        }

        return new CommitStockUseCase(new OrderId(payload.orderId()));
    }
}
