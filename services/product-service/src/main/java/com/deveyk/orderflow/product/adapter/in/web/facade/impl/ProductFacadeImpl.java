package com.deveyk.orderflow.product.adapter.in.web.facade.impl;

import com.deveyk.orderflow.product.adapter.in.web.facade.ProductFacade;
import com.deveyk.orderflow.product.adapter.in.web.mapper.ProductRequestToUseCaseMapper;
import com.deveyk.orderflow.product.adapter.in.web.mapper.ProductToResponseMapper;
import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.response.ProductResponse;
import com.deveyk.orderflow.product.application.product.usecase.ChangeProductDetailsUseCase;
import com.deveyk.orderflow.product.application.product.usecase.ListProductsUseCase;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class ProductFacadeImpl implements ProductFacade {

    private final UseCaseHandler<List<Product>, ListProductsUseCase> listProductsUseCaseHandler;
    private final UseCaseHandler<Product, ChangeProductDetailsUseCase> changeProductDetailsUseCaseHandler;

    @Override
    public List<ProductResponse> listProducts() {
        return listProductsUseCaseHandler.handle(ProductRequestToUseCaseMapper.toListProductsUseCase()).stream()
                .map(ProductToResponseMapper::toProductResponse)
                .toList();
    }

    @Override
    public ProductResponse changeProductDetails(final UUID productId, final ChangeProductDetailsRequest request) {
        final Product product = changeProductDetailsUseCaseHandler.handle(ProductRequestToUseCaseMapper.toChangeProductDetailsUseCase(productId, request));
        return ProductToResponseMapper.toProductResponse(product);
    }
}
