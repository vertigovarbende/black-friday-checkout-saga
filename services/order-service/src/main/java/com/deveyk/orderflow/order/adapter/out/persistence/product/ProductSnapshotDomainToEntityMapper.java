package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;

final class ProductSnapshotDomainToEntityMapper {

    private ProductSnapshotDomainToEntityMapper() {
    }

    static ProductSnapshotEntity toNewEntity(final ProductSnapshot productSnapshot) {
        return ProductSnapshotEntity.builder()
                .id(productSnapshot.getId().value())
                .name(productSnapshot.getName())
                .unitPrice(productSnapshot.getUnitPrice().amount())
                .currency(productSnapshot.getUnitPrice().currency().getCurrencyCode())
                .productVersion(productSnapshot.getProductVersion())
                .build();
    }
}
