package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;

// Üretici tarafı; tüketici tarafı order-service'in ProductChangedContractTest'i. İkisi aynı golden JSON'a karşı.
class ProductChangedContractTest {

    @Test
    @DisplayName("product.changed zarfı golden JSON sözleşmesiyle (contracts/product.changed.v1.json) birebir aynıdır; productVersion = catalogVersion")
    void givenChangedProduct_whenSerializeProductChanged_thenMatchesGoldenContract() {
        // Given
        final Product product = new ProductBuilder()
                .withValidValues()
                .withName("Wireless Headphones Pro")
                .withUnitPrice(new Money(new BigDecimal("279.90"), TRY))
                .withCatalogVersion(3L)
                .build();

        final EventMetadata metadata = new EventMetadata(UUID.fromString("0198a1b2-0000-7000-8000-00000000e101"),
                EventDefinitions.PRODUCT_CHANGED.eventType(), EventDefinitions.PRODUCT_CHANGED.aggregateType(),
                ProductValidTestData.Product.ID.toString(), EventSourceNames.PRODUCT_SERVICE, "1",
                Instant.parse("2026-11-27T10:00:00Z"), "4bf92f3577b34da6a3ce929d0e0e4736", "4bf92f3577b34da6a3ce929d0e0e4736");

        // When
        final String json = JsonMapper.builder().build()
                .writeValueAsString(new OrderflowEvent<>(metadata, ProductToProductChangedPayloadMapper.toPayload(product)));

        // Then
        GoldenJson.assertMatches("product.changed.v1", json);
    }
}
