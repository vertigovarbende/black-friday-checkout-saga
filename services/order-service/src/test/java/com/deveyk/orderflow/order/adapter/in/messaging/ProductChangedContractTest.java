package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.messaging.AggregateTypeNames;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.testsupport.contract.GoldenJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

// Tüketici tarafı: golden sözleşmeyi okuyabiliyor muyuz? Üretici tarafı (assertMatches) product-service'te, Faz 3.
class ProductChangedContractTest {

    @Test
    @DisplayName("product.changed golden JSON sözleşmesi (contracts/product.changed.v1.json) okunur ve use case'e çevrilir")
    void givenGoldenContract_whenReadAndMap_thenUseCaseMatches() {
        // Given
        final String json = GoldenJson.read("product.changed.v1");

        // When
        final OrderflowEvent<ProductChangedPayload> event = new EventJsonReader(JsonMapper.builder().build())
                .read(json, ProductChangedPayload.class);
        final ApplyProductChangeUseCase useCase = ProductChangedPayloadToUseCaseMapper.toUseCase(event.payload());

        // Then
        assertThat(event.metadata().eventType()).isEqualTo(EventTypeNames.PRODUCT_CHANGED);
        assertThat(event.metadata().aggregateType()).isEqualTo(AggregateTypeNames.PRODUCT);
        assertThat(event.metadata().source()).isEqualTo(EventSourceNames.PRODUCT_SERVICE);
        assertThat(event.metadata().aggregateId()).isEqualTo(OrderValidTestData.Product.ID.toString());

        assertThat(useCase.productId()).isEqualTo(OrderValidTestData.Product.PRODUCT_ID);
        assertThat(useCase.name()).isEqualTo("Wireless Headphones Pro");
        assertThat(useCase.unitPrice()).isEqualTo(new Money(new BigDecimal("279.90"), TRY));
        assertThat(useCase.productVersion()).isEqualTo(3L);
    }
}
