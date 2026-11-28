package com.deveyk.orderflow.product.application.reservation.port;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;

import java.util.Optional;

public interface StockReservationReadPort {

    Optional<StockReservation> findById(OrderId orderId);
}
