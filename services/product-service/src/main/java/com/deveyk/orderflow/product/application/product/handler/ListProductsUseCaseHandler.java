package com.deveyk.orderflow.product.application.product.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.usecase.ListProductsUseCase;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListProductsUseCaseHandler implements UseCaseHandler<List<Product>, ListProductsUseCase> {

    private final ProductReadPort productReadPort;

    // readOnly → replica (ADR-0004).
    @Override
    @Transactional(readOnly = true)
    public List<Product> handle(final ListProductsUseCase useCase) {
        return productReadPort.findAll();
    }
}
