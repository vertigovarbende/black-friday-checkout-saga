package com.deveyk.orderflow.order.application.order.port;

import com.deveyk.orderflow.order.domain.order.model.Order;

public interface OrderSavePort {

    // Order.place ile yeni oluşturulmuş sipariş.
    void create(Order order);

    // Daha önce okunmuş ve değiştirilmiş sipariş. Okunduğu sürüm artık güncel değilse
    // OptimisticLockingFailureException; çağıran bir transaction içinde olmalıdır.
    void update(Order order);
}
