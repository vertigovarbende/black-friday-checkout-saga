package com.deveyk.orderflow.product.application.reservation.port;

import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;

// Saga'ya cevaplar (event-catalog.md §2.2).
public interface StockSagaEventPort {

    void publishStockReserved(StockReservation reservation);

    void publishStockReserveFailed(OrderId orderId, StockRejection rejection);

    void publishStockReleased(StockReservation reservation);
}
