package com.deveyk.orderflow.product.application.product.port;

import com.deveyk.orderflow.product.domain.product.model.Product;

public interface ProductSavePort {

    // Daha önce okunmuş ürün. Okunduğu sürüm artık güncel değilse OptimisticLockingFailureException; çağıran bir
    // transaction içinde olmalıdır. Değişmemiş ürün için bir şey yazılmaz.
    void update(Product product);
}
