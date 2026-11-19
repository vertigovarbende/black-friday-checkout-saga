package com.deveyk.orderflow.order.domain.product.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ProductSnapshotBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSnapshotTest {

    @Test
    @DisplayName("Geçerli alanlarla ürün kopyası kurulur")
    void givenValidValues_whenRehydrate_thenFieldsSet() {
        // When
        final ProductSnapshot product = new ProductSnapshotBuilder()
                .withValidValues()
                .build();

        // Then
        assertThat(product.getId()).isEqualTo(new ProductId(OrderValidTestData.Product.ID));
        assertThat(product.getName()).isEqualTo(OrderValidTestData.Product.NAME);
        assertThat(product.getUnitPrice()).isEqualTo(new Money(OrderValidTestData.Product.UNIT_PRICE, TRY));
        assertThat(product.getVersion()).isZero();
    }

    @Test
    @DisplayName("Kimliği olmayan ürün kopyası kurulamaz")
    void givenNoId_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductSnapshotBuilder().withValidValues().withId(null).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.PRODUCT_ID_REQUIRED));
    }

    @Test
    @DisplayName("Adı boş olan ürün kopyası kurulamaz")
    void givenBlankName_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductSnapshotBuilder().withValidValues().withName(" ").build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.PRODUCT_NAME_REQUIRED));
    }

    @Test
    @DisplayName("Adı en fazla 200 karakter olabilir; sipariş satırının sınırıyla aynı")
    void givenTooLongName_whenRehydrate_thenRejected() {
        // Given
        final String name = "x".repeat(ProductSnapshot.MAX_NAME_LENGTH + 1);

        // When / Then
        assertThatThrownBy(() -> new ProductSnapshotBuilder().withValidValues().withName(name).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.PRODUCT_NAME_TOO_LONG));
    }

    @Test
    @DisplayName("Fiyatı olmayan ürün kopyası kurulamaz")
    void givenNoUnitPrice_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductSnapshotBuilder().withValidValues().withUnitPrice(null).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.UNIT_PRICE_REQUIRED));
    }

    @Test
    @DisplayName("Fiyatı sıfır olan ürün kopyası kurulamaz")
    void givenZeroUnitPrice_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductSnapshotBuilder().withValidValues()
                .withUnitPrice(new Money(BigDecimal.ZERO, TRY)).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.UNIT_PRICE_MUST_BE_POSITIVE));
    }
}
