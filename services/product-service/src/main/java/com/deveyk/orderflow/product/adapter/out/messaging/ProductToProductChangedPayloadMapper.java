package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;

final class ProductToProductChangedPayloadMapper {

    private ProductToProductChangedPayloadMapper() {
    }

    // productVersion = catalogVersion: sadece ad ya da fiyat değişince artar (Product).
    static ProductChangedPayload toPayload(final Product product) {
        return ProductChangedPayload.builder()
                .productId(product.getId().value())
                .name(product.getName())
                .unitPrice(MoneyAmount.of(product.getUnitPrice().amount(), product.getUnitPrice().currency()))
                .productVersion(product.getCatalogVersion())
                .build();
    }
}
