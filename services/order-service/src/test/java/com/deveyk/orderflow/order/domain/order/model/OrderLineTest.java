package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderLineTest {

    @Test
    @DisplayName("Yeni satır kimliğini kendisi üretir ve satır toplamı birim fiyat × adettir")
    void givenValidValues_whenCreate_thenIdAssignedAndLineTotalComputed() {
        // When
        final OrderLine line = OrderLine.create(
                OrderValidTestData.Product.PRODUCT_ID,
                OrderValidTestData.Product.NAME,
                OrderValidTestData.Product.UNIT_PRICE_MONEY,
                new Quantity(2)
        );

        // Then
        assertThat(line.getId()).isNotNull();
        assertThat(line.getLineTotal()).isEqualTo(new Money(new BigDecimal("499.80"), TRY));
    }

    @Test
    @DisplayName("Ürünü olmayan satır oluşturulamaz")
    void givenNoProduct_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderLineBuilder().withValidValues().withProductId(null).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.PRODUCT_REQUIRED));
    }

    @Test
    @DisplayName("Ürün adı boş olan satır oluşturulamaz")
    void givenBlankProductName_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderLineBuilder().withValidValues().withProductName("  ").build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.PRODUCT_NAME_REQUIRED));
    }

    @Test
    @DisplayName("Ürün adı MAX_PRODUCT_NAME_LENGTH karakterden uzun olamaz")
    void givenTooLongProductName_whenCreate_thenRejected() {
        // Given
        final String name = "x".repeat(OrderLine.MAX_PRODUCT_NAME_LENGTH + 1);

        // When / Then
        assertThatThrownBy(() -> new OrderLineBuilder().withValidValues().withProductName(name).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.PRODUCT_NAME_TOO_LONG));
    }

    @Test
    @DisplayName("Birim fiyatı sıfır olan satır oluşturulamaz")
    void givenZeroUnitPrice_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderLineBuilder().withValidValues().withUnitPrice(new Money(BigDecimal.ZERO, TRY)).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.UNIT_PRICE_MUST_BE_POSITIVE));
    }

    @Test
    @DisplayName("Adedi sıfır olan satır oluşturulamaz")
    void givenZeroQuantity_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderLineBuilder().withValidValues().withQuantity(new Quantity(0)).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.QUANTITY_MUST_BE_POSITIVE));
    }
}
