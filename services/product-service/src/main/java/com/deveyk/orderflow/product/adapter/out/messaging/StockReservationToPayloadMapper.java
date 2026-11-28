package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleasedPayload;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveFailedPayload;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReservedPayload;

final class StockReservationToPayloadMapper {

    private StockReservationToPayloadMapper() {
    }

    static StockReservedPayload toReservedPayload(final StockReservation reservation) {
        return StockReservedPayload.builder()
                .orderId(reservation.getId().value())
                .reservedAt(reservation.getCreatedAt())
                .build();
    }

    static StockReserveFailedPayload toReserveFailedPayload(final OrderId orderId, final StockRejection rejection) {
        return StockReserveFailedPayload.builder()
                .orderId(orderId.value())
                .reason(rejection.reason().name())
                .productId(rejection.productId() == null ? null : rejection.productId().value())
                .build();
    }

    static StockReleasedPayload toReleasedPayload(final StockReservation reservation) {
        return StockReleasedPayload.builder()
                .orderId(reservation.getId().value())
                .releasedAt(reservation.getLastModifiedAt())
                .build();
    }
}
