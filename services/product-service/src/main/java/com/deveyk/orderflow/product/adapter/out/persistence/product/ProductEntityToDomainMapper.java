package com.deveyk.orderflow.product.adapter.out.persistence.product;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;

import java.util.Currency;

final class ProductEntityToDomainMapper {

    private ProductEntityToDomainMapper() {
    }

    static Product toDomain(final ProductEntity entity) {
        return Product.rehydrate()
                .id(new ProductId(entity.getId()))
                .name(entity.getName())
                .unitPrice(new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency())))
                .onHand(new Quantity(entity.getOnHand()))
                .reserved(new Quantity(entity.getReserved()))
                .catalogVersion(entity.getCatalogVersion())
                .version(entity.getVersion())
                .build();
    }
}
