package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotSavePort;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ProductSnapshotAdapter implements ProductSnapshotReadPort, ProductSnapshotSavePort {

    private final ProductSnapshotRepository productSnapshotRepository;

    @Override
    public List<ProductSnapshot> findAllByIds(final Collection<ProductId> productIds) {
        return productSnapshotRepository.findAllById(productIds.stream().map(ProductId::value).toList()).stream()
                .map(ProductSnapshotEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ProductSnapshot> findById(final ProductId productId) {
        return productSnapshotRepository.findById(productId.value()).map(ProductSnapshotEntityToDomainMapper::toDomain);
    }

    @Override
    public void create(final ProductSnapshot productSnapshot) {
        productSnapshotRepository.save(ProductSnapshotDomainToEntityMapper.toNewEntity(productSnapshot));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final ProductSnapshot productSnapshot) {

        final ProductSnapshotEntity entity = productSnapshotRepository.findById(productSnapshot.getId().value())
                .orElseThrow(() -> new OptimisticLockingFailureException("Product snapshot " + productSnapshot.getId().value() + " was deleted concurrently"));

        if (entity.getVersion() != productSnapshot.getVersion()) {

            String msg = "Product snapshot " + productSnapshot.getId().value() + " was modified concurrently: read version " + productSnapshot.getVersion() + ", current version " + entity.getVersion();
            throw new OptimisticLockingFailureException(msg);

        }

        entity.applyState(productSnapshot.getName(), productSnapshot.getUnitPrice().amount(), productSnapshot.getUnitPrice().currency().getCurrencyCode(), productSnapshot.getProductVersion());
    }
}
