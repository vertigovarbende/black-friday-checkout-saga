package com.deveyk.orderflow.product.adapter.in.web.mapper;

import com.deveyk.orderflow.product.adapter.in.web.response.MoneyResponse;
import com.deveyk.orderflow.product.adapter.in.web.response.ProductResponse;
import com.deveyk.orderflow.product.domain.product.model.Product;

public final class ProductToResponseMapper {

    private ProductToResponseMapper() {
    }

    public static ProductResponse toProductResponse(final Product product) {
        return ProductResponse.builder()
                .productId(product.getId().value())
                .name(product.getName())
                .unitPrice(MoneyResponse.builder()
                        .amount(product.getUnitPrice().amount().toPlainString())
                        .currency(product.getUnitPrice().currency().getCurrencyCode())
                        .build())
                .onHand(product.getOnHand().value())
                .reserved(product.getReserved().value())
                .available(product.available().value())
                .catalogVersion(product.getCatalogVersion())
                .build();
    }
}
