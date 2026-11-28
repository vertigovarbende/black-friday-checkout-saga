package com.deveyk.orderflow.order.application.product.port;

import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductSnapshotReadPort {

    List<ProductSnapshot> findAllByIds(Collection<ProductId> productIds);

    Optional<ProductSnapshot> findById(ProductId productId);
}
