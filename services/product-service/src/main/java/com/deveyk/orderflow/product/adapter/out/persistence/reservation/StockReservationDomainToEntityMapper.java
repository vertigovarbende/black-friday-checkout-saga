package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;

final class StockReservationDomainToEntityMapper {

    private StockReservationDomainToEntityMapper() {
    }

    static StockReservationEntity toNewEntity(final StockReservation reservation) {

        final StockRejection rejection = reservation.getRejection().orElse(null);

        final StockReservationEntity entity = StockReservationEntity.builder()
                .id(reservation.getId().value())
                .status(reservation.getStatus())
                .rejectionReason(rejection == null ? null : rejection.reason())
                .rejectedProductId(rejection == null ? null : rejection.productId().value())
                .requestedAt(reservation.getCreatedAt())
                .lastModifiedAt(reservation.getLastModifiedAt())
                .build();

        reservation.getLines().forEach(line ->
                entity.addLine(new StockReservationLineEmbeddable(line.productId().value(), line.quantity().value())));
        return entity;
    }
}
