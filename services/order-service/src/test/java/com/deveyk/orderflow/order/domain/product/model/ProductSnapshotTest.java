package com.deveyk.orderflow.order.domain.product.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ProductSnapshotBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @DisplayName("Yeni kopya product.changed'deki ürün sürümüyle oluşturulur; kendi optimistic lock sürümü 0'dır")
    void givenValidValues_whenCreate_thenFieldsAndVersionsSet() {
        // When
        final ProductSnapshot product = ProductSnapshot.create(new ProductId(OrderValidTestData.Product.ID),
                OrderValidTestData.Product.NAME, new Money(OrderValidTestData.Product.UNIT_PRICE, TRY), 3L);

        // Then
        assertThat(product.getName()).isEqualTo(OrderValidTestData.Product.NAME);
        assertThat(product.getUnitPrice()).isEqualTo(new Money(OrderValidTestData.Product.UNIT_PRICE, TRY));
        assertThat(product.getProductVersion()).isEqualTo(3L);
        assertThat(product.getVersion()).isZero();
    }

    @Test
    @DisplayName("Negatif ürün sürümüyle kopya oluşturulamaz")
    void givenNegativeProductVersion_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> ProductSnapshot.create(new ProductId(OrderValidTestData.Product.ID),
                OrderValidTestData.Product.NAME, new Money(OrderValidTestData.Product.UNIT_PRICE, TRY), -1L))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.PRODUCT_VERSION_NEGATIVE));
    }

    @Test
    @DisplayName("Daha yeni sürüm uygulanır: ad, fiyat ve ürün sürümü değişir")
    void givenNewerVersion_whenApplyChange_thenApplied() {
        // Given
        final ProductSnapshot product = new ProductSnapshotBuilder().withValidValues().withProductVersion(2L).build();
        final Money newPrice = new Money(new BigDecimal("279.90"), TRY);

        // When
        final boolean applied = product.applyChange("Wireless Headphones Pro", newPrice, 3L);

        // Then
        assertThat(applied).isTrue();
        assertThat(product.getName()).isEqualTo("Wireless Headphones Pro");
        assertThat(product.getUnitPrice()).isEqualTo(newPrice);
        assertThat(product.getProductVersion()).isEqualTo(3L);
    }

    @ParameterizedTest(name = "mevcut sürüm 2, gelen sürüm {0}")
    @ValueSource(longs = {2L, 1L, 0L})
    @DisplayName("Aynı ya da eski sürüm (yeniden yayın, geç gelen kopya) hiçbir şeyi değiştirmez")
    void givenSameOrOlderVersion_whenApplyChange_thenIgnored(final long incomingVersion) {
        // Given
        final ProductSnapshot product = new ProductSnapshotBuilder().withValidValues().withProductVersion(2L).build();

        // When
        final boolean applied = product.applyChange("Old Name", new Money(new BigDecimal("1.00"), TRY), incomingVersion);

        // Then
        assertThat(applied).isFalse();
        assertThat(product.getName()).isEqualTo(OrderValidTestData.Product.NAME);
        assertThat(product.getUnitPrice()).isEqualTo(new Money(OrderValidTestData.Product.UNIT_PRICE, TRY));
        assertThat(product.getProductVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Daha yeni sürüm de kuralları geçmelidir; geçmezse kopya değişmez")
    void givenNewerVersionWithBlankName_whenApplyChange_thenRejectedAndUnchanged() {
        // Given
        final ProductSnapshot product = new ProductSnapshotBuilder().withValidValues().withProductVersion(2L).build();

        // When / Then
        assertThatThrownBy(() -> product.applyChange(" ", new Money(new BigDecimal("279.90"), TRY), 3L))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductSnapshotErrorCode.PRODUCT_NAME_REQUIRED));
        assertThat(product.getName()).isEqualTo(OrderValidTestData.Product.NAME);
        assertThat(product.getProductVersion()).isEqualTo(2L);
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
