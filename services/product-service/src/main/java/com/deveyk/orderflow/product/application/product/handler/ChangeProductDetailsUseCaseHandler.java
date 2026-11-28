package com.deveyk.orderflow.product.application.product.handler;

import com.deveyk.orderflow.product.application.product.port.ProductDomainEventPort;
import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.product.usecase.ChangeProductDetailsUseCase;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.product.exception.ProductNotFoundException;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeProductDetailsUseCaseHandler implements UseCaseHandler<Product, ChangeProductDetailsUseCase> {

    private final ProductReadPort productReadPort;
    private final ProductSavePort productSavePort;
    private final ProductDomainEventPort productDomainEventPort;

    // Ürün ve product.changed'in outbox kaydı aynı transaction'da.
    @Override
    @Transactional
    public Product handle(final ChangeProductDetailsUseCase useCase) {

        final Product product = productReadPort.findById(useCase.productId())
                .orElseThrow(() -> new ProductNotFoundException(useCase.productId()));

        final String name = useCase.name() == null ? product.getName() : useCase.name();
        final Money unitPrice = useCase.unitPrice() == null ? product.getUnitPrice() : useCase.unitPrice();

        if (!product.changeDetails(name, unitPrice)) {
            log.info("Product details unchanged productId={} catalogVersion={}", product.getId().value(), product.getCatalogVersion());
            return product;
        }

        productSavePort.update(product);
        productDomainEventPort.publishProductChanged(product);

        log.info("Product details changed productId={} catalogVersion={}", product.getId().value(), product.getCatalogVersion());
        return product;
    }
}
