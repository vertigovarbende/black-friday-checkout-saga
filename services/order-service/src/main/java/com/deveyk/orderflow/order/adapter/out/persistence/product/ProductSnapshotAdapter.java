package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
class ProductSnapshotAdapter implements ProductSnapshotReadPort {

    private final ProductSnapshotRepository productSnapshotRepository;

    @Override
    public List<ProductSnapshot> findAllByIds(final Collection<ProductId> productIds) {
        return productSnapshotRepository.findAllById(productIds.stream().map(ProductId::value).toList()).stream()
                .map(ProductSnapshotEntityToDomainMapper::toDomain)
                .toList();
    }
}
