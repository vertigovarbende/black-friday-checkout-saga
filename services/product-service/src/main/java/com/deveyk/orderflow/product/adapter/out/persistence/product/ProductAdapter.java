package com.deveyk.orderflow.product.adapter.out.persistence.product;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.exception.ProductNotFoundException;
import com.deveyk.orderflow.product.domain.product.model.Product;
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
class ProductAdapter implements ProductReadPort, ProductSavePort {

    private final ProductRepository productRepository;

    @Override
    public Optional<Product> findById(final ProductId productId) {
        return productRepository.findById(productId.value()).map(ProductEntityToDomainMapper::toDomain);
    }

    @Override
    public List<Product> findAllByIds(final Collection<ProductId> productIds) {
        return productRepository.findAllById(productIds.stream().map(ProductId::value).toList()).stream()
                .map(ProductEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAllByOrderByNameAsc().stream()
                .map(ProductEntityToDomainMapper::toDomain)
                .toList();
    }

    // Transaction dışında çağrılırsa entity hemen detach olur ve değişiklik sessizce kaybolur; bu yüzden MANDATORY.
    // Değişmemiş ürün için Hibernate UPDATE yazmaz.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void update(final Product product) {

        final ProductEntity entity = productRepository.findById(product.getId().value())
                .orElseThrow(() -> new ProductNotFoundException(product.getId()));

        if (entity.getVersion() != product.getVersion()) {

            String msg = "Product " + product.getId().value() + " was modified concurrently: read version " + product.getVersion() + ", current version " + entity.getVersion();
            throw new OptimisticLockingFailureException(msg);

        }

        entity.applyState(product.getName(), product.getUnitPrice().amount(), product.getUnitPrice().currency().getCurrencyCode(),
                product.getOnHand().value(), product.getReserved().value(), product.getCatalogVersion());
    }
}
