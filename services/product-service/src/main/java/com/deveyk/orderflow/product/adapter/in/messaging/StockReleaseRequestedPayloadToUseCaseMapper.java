package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.ReleaseStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleaseRequestedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;

final class StockReleaseRequestedPayloadToUseCaseMapper {

    private StockReleaseRequestedPayloadToUseCaseMapper() {
    }

    static ReleaseStockUseCase toUseCase(final StockReleaseRequestedPayload payload) {

        if (payload.orderId() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.orderId");
        }

        return new ReleaseStockUseCase(new OrderId(payload.orderId()));
    }
}
