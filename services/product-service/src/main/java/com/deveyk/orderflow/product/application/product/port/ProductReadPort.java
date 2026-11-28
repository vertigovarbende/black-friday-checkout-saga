package com.deveyk.orderflow.product.application.product.port;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.model.Product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ProductReadPort {

    Optional<Product> findById(ProductId productId);

    // Bulunamayan id'ler sonuçta yoktur; karar (UNKNOWN_PRODUCT) domain'dedir.
    List<Product> findAllByIds(Collection<ProductId> productIds);

    List<Product> findAll();

    default Map<ProductId, Product> findAllByIdsAsMap(final Collection<ProductId> productIds) {
        return findAllByIds(productIds).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
