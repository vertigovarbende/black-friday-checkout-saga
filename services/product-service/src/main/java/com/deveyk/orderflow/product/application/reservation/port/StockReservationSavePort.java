package com.deveyk.orderflow.product.application.reservation.port;

import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;

public interface StockReservationSavePort {

    // StockReservation.request ya da closed ile yeni oluşturulmuş rezervasyon.
    void create(StockReservation reservation);

    // Daha önce okunmuş ve değiştirilmiş rezervasyon. Okunduğu sürüm artık güncel değilse
    // OptimisticLockingFailureException; çağıran bir transaction içinde olmalıdır.
    void update(StockReservation reservation);
}
