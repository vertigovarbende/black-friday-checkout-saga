package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationLine;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;

final class StockReservationEntityToDomainMapper {

    private StockReservationEntityToDomainMapper() {
    }

    static StockReservation toDomain(final StockReservationEntity entity) {
        return StockReservation.rehydrate()
                .id(new OrderId(entity.getId()))
                .lines(entity.getLines().stream()
                        .map(line -> new ReservationLine(new ProductId(line.getProductId()), new Quantity(line.getQuantity())))
                        .toList())
                .status(entity.getStatus())
                .rejection(entity.getRejectionReason() == null ? null
                        : new StockRejection(entity.getRejectionReason(), new ProductId(entity.getRejectedProductId())))
                .createdAt(entity.getRequestedAt())
                .lastModifiedAt(entity.getLastModifiedAt())
                .version(entity.getVersion())
                .build();
    }
}
