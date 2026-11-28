package com.deveyk.orderflow.product.application.product.port;

import com.deveyk.orderflow.product.domain.product.model.Product;

public interface ProductDomainEventPort {

    // product.changed: ad ya da fiyat değişti; productVersion = catalogVersion.
    void publishProductChanged(Product product);
}
