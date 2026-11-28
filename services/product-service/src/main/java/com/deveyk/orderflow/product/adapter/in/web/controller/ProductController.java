package com.deveyk.orderflow.product.adapter.in.web.controller;

import com.deveyk.orderflow.product.adapter.in.web.facade.ProductFacade;
import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Admin endpoint'leri: X-User-Id istenmez (02 §4.1).
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
class ProductController {

    static final String PRODUCT_PATH = "/{productId}";

    private final ProductFacade productFacade;

    @GetMapping
    ResponseEntity<List<ProductResponse>> listProducts() {
        return ResponseEntity.ok(productFacade.listProducts());
    }

    // Ad ya da fiyat değişirse product.changed yayınlanır (outbox).
    @PatchMapping(PRODUCT_PATH)
    ResponseEntity<ProductResponse> changeProductDetails(@PathVariable final UUID productId,
                                                         @Valid @RequestBody final ChangeProductDetailsRequest request) {
        return ResponseEntity.ok(productFacade.changeProductDetails(productId, request));
    }
}
