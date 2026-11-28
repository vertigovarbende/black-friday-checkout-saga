package com.deveyk.orderflow.product.adapter.in.web.controller;

import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.request.MoneyRequest;
import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.product.exception.ProductErrorCode;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvcRequestBuilders;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockResultMatchersBuilders;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerIT extends ProductTestContainerConfiguration {

    private static final String BASE_PATH = "/api/v1/products";
    private static final UUID PRODUCT_ID = ProductValidTestData.Product.ID;

    @Autowired
    private ProductReadPort productReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Ürünler ada göre sıralı, stoklarıyla listelenir")
    void givenProducts_whenListProducts_thenSortedWithStock() throws Exception {
        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(ProductValidTestData.SecondProduct.NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(ProductValidTestData.Product.NAME))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].available").value(ProductValidTestData.Product.ON_HAND.value()));
    }

    @Test
    @DisplayName("Fiyat değişir: ürün kaydedilir, katalog sürümü artar ve product.changed outbox'a yazılır")
    void givenNewPrice_whenChangeProductDetails_thenSavedAndProductChangedInOutbox() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest(null, new MoneyRequest(new BigDecimal("279.90"), "TRY"));

        // When
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(OrderflowMockResultMatchersBuilders.response("catalogVersion").value(2));

        // Then
        final Product saved = productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow();
        assertThat(saved.getUnitPrice()).isEqualTo(new Money(new BigDecimal("279.90"), TRY));
        assertThat(saved.getName()).isEqualTo(ProductValidTestData.Product.NAME);

        final OutboxAssert.OutboxRow event = OutboxAssert.of(jdbcTemplate).hasEvent(PRODUCT_ID.toString(), EventDefinitions.PRODUCT_CHANGED.eventType());
        assertThat(event.topic()).isEqualTo(TopicNames.PRODUCT_CHANGED);
        assertThat(JsonPath.<Integer>read(event.payload(), "$.payload.productVersion")).isEqualTo(2);
        assertThat(JsonPath.<String>read(event.payload(), "$.payload.unitPrice.amount")).isEqualTo("279.90");
    }

    @Test
    @DisplayName("Aynı ad ve fiyat değişiklik değildir: 200, katalog sürümü 1 kalır, outbox'a bir şey yazılmaz")
    void givenSameDetails_whenChangeProductDetails_thenNoEvent() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest(ProductValidTestData.Product.NAME,
                new MoneyRequest(ProductValidTestData.Product.UNIT_PRICE, "TRY"));

        // When
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(OrderflowMockResultMatchersBuilders.response("catalogVersion").value(1));

        // Then
        OutboxAssert.of(jdbcTemplate).hasNoEvent(PRODUCT_ID.toString());
    }

    @Test
    @DisplayName("Boş ad domain kuralıdır: 400 PRD_003, ürün değişmez")
    void givenBlankName_whenChangeProductDetails_thenValidationErrorAndUnchanged() throws Exception {
        // Given
        final ChangeProductDetailsRequest request = new ChangeProductDetailsRequest(" ", null);

        // When
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.patch(BASE_PATH + "/" + PRODUCT_ID, request), ProductErrorCode.PRODUCT_NAME_REQUIRED)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Then
        assertThat(productReadPort.findById(ProductValidTestData.Product.PRODUCT_ID).orElseThrow().getName())
                .isEqualTo(ProductValidTestData.Product.NAME);
        OutboxAssert.of(jdbcTemplate).hasNoEvent(PRODUCT_ID.toString());
    }
}
