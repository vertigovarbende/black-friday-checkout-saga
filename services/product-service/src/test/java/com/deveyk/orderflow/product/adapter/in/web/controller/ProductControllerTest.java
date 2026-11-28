package com.deveyk.orderflow.product.adapter.in.web.controller;

import com.deveyk.orderflow.product.adapter.in.web.facade.ProductFacade;
import com.deveyk.orderflow.product.adapter.in.web.mapper.ProductToResponseMapper;
import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.request.MoneyRequest;
import com.deveyk.orderflow.product.domain.product.exception.ProductErrorCode;
import com.deveyk.orderflow.product.domain.product.exception.ProductNotFoundException;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.shared.testsupport.OrderflowRestControllerTest;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvcRequestBuilders;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockResultMatchersBuilders;
import com.deveyk.orderflow.shared.web.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@WebMvcTest(ProductController.class)
class ProductControllerTest extends OrderflowRestControllerTest {

    private static final String BASE_PATH = "/api/v1/products";
    private static final UUID PRODUCT_ID = ProductValidTestData.Product.ID;

    @MockitoBean
    private ProductFacade productFacade;

    @Test
    @DisplayName("Ürün listesi 200 döner; her üründe fiyat ve stok bilgisi var")
    void givenProducts_whenListProducts_thenOkWithStock() throws Exception {
        // Given
        Mockito.when(productFacade.listProducts()).thenReturn(List.of(ProductToResponseMapper.toProductResponse(
                new ProductBuilder().withValidValues().withOnHand(10).withReserved(3).build())));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].productId").value(PRODUCT_ID.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].unitPrice.amount").value("249.90"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].reserved").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].available").value(7));
    }

    @Test
    @DisplayName("Geçerli PATCH 200 döner ve güncel ürünü gösterir")
    void givenValidRequest_whenChangeProductDetails_thenOkWithProduct() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest(null, new MoneyRequest(new BigDecimal("279.90"), "TRY"));
        Mockito.when(productFacade.changeProductDetails(PRODUCT_ID, request)).thenReturn(ProductToResponseMapper.toProductResponse(
                new ProductBuilder().withValidValues().withCatalogVersion(2L).build()));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(OrderflowMockResultMatchersBuilders.response("catalogVersion").value(2));

        // Verify
        Mockito.verify(productFacade).changeProductDetails(PRODUCT_ID, request);
    }

    @Test
    @DisplayName("Tutarı olmayan fiyat 400 GEN_002 ile reddedilir")
    void givenPriceWithoutAmount_whenChangeProductDetails_thenValidationError() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest(null, new MoneyRequest(null, "TRY"));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request), CommonErrorCode.VALIDATION_ERROR)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Verify
        Mockito.verifyNoInteractions(productFacade);
    }

    @Test
    @DisplayName("Bilinmeyen ürün 404 PRD_001 döner")
    void givenUnknownProduct_whenChangeProductDetails_thenNotFound() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest("Studio Monitor", null);
        Mockito.when(productFacade.changeProductDetails(PRODUCT_ID, request))
                .thenThrow(new ProductNotFoundException(ProductValidTestData.Product.PRODUCT_ID));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request), ProductErrorCode.PRODUCT_NOT_FOUND)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isNotFound());
    }
}
