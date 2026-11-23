package com.deveyk.orderflow.order.application.product.port;

import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;

public interface ProductSnapshotSavePort {

    // ProductSnapshot.create ile yeni oluşturulmuş kopya.
    void create(ProductSnapshot productSnapshot);

    // Daha önce okunmuş ve değiştirilmiş kopya. Okunduğu sürüm artık güncel değilse
    // OptimisticLockingFailureException; çağıran bir transaction içinde olmalıdır.
    void update(ProductSnapshot productSnapshot);
}
