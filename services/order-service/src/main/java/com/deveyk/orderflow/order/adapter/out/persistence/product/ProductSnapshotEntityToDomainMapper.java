package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;

import java.util.Currency;

final class ProductSnapshotEntityToDomainMapper {

    private ProductSnapshotEntityToDomainMapper() {
    }

    static ProductSnapshot toDomain(final ProductSnapshotEntity entity) {
        return ProductSnapshot.rehydrate()
                .id(new ProductId(entity.getId()))
                .name(entity.getName())
                .unitPrice(new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency())))
                .productVersion(entity.getProductVersion())
                .version(entity.getVersion())
                .build();
    }
}
