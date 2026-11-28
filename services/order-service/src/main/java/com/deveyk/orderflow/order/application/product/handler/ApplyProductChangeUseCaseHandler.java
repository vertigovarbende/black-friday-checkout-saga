package com.deveyk.orderflow.order.application.product.handler;

import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotSavePort;
import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyProductChangeUseCaseHandler implements UseCaseHandler<ProductSnapshot, ApplyProductChangeUseCase> {

    private final ProductSnapshotReadPort productSnapshotReadPort;
    private final ProductSnapshotSavePort productSnapshotSavePort;

    // Listener'dan inbox'ın transaction'ı içinde çağrılır; inbox kaydı ve kopya birlikte commit edilir.
    @Override
    @Transactional
    public ProductSnapshot handle(final ApplyProductChangeUseCase useCase) {

        final Optional<ProductSnapshot> existing = productSnapshotReadPort.findById(useCase.productId());

        if (existing.isEmpty()) {

            final ProductSnapshot created = ProductSnapshot.create(useCase.productId(), useCase.name(), useCase.unitPrice(), useCase.productVersion());
            productSnapshotSavePort.create(created);
            log.info("Product snapshot created productId={} productVersion={}", created.getId().value(), created.getProductVersion());
            return created;

        }

        final ProductSnapshot product = existing.get();

        if (!product.applyChange(useCase.name(), useCase.unitPrice(), useCase.productVersion())) {

            log.info("Stale product change skipped productId={} productVersion={} currentProductVersion={}", product.getId().value(), useCase.productVersion(), product.getProductVersion());
            return product;

        }

        productSnapshotSavePort.update(product);
        log.info("Product snapshot updated productId={} productVersion={}", product.getId().value(), product.getProductVersion());
        return product;
    }
}
