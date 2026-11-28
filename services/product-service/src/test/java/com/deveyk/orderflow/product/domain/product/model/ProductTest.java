package com.deveyk.orderflow.product.domain.product.model;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.exception.InsufficientStockException;
import com.deveyk.orderflow.product.domain.product.exception.ProductErrorCode;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("Geçerli alanlarla ürün kurulur; kullanılabilir stok onHand − reserved")
    void givenValidValues_whenRehydrate_thenFieldsAndAvailableStock() {
        // When
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(3).build();

        // Then
        assertThat(product.getId()).isEqualTo(ProductValidTestData.Product.PRODUCT_ID);
        assertThat(product.getName()).isEqualTo(ProductValidTestData.Product.NAME);
        assertThat(product.getUnitPrice()).isEqualTo(ProductValidTestData.Product.UNIT_PRICE_MONEY);
        assertThat(product.getCatalogVersion()).isEqualTo(1L);
        assertThat(product.available()).isEqualTo(new Quantity(7));
    }

    @Test
    @DisplayName("Ayrılmış stok eldeki stoktan fazla olamaz")
    void givenReservedAboveOnHand_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductBuilder().withValidValues().withOnHand(2).withReserved(3).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.RESERVED_EXCEEDS_ON_HAND));
    }

    @Test
    @DisplayName("Katalog sürümü 1'den başlar")
    void givenCatalogVersionZero_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new ProductBuilder().withValidValues().withCatalogVersion(0L).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.CATALOG_VERSION_INVALID));
    }

    @ParameterizedTest(name = "fiyat {0}")
    @ValueSource(strings = {"0", "0.00"})
    @DisplayName("Fiyat sıfırdan büyük olmalı")
    void givenNonPositivePrice_whenRehydrate_thenRejected(final String price) {
        // When / Then
        assertThatThrownBy(() -> new ProductBuilder().withValidValues().withUnitPrice(new Money(new BigDecimal(price), TRY)).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.UNIT_PRICE_MUST_BE_POSITIVE));
    }

    @Test
    @DisplayName("Reserve kullanılabilir stoğu azaltır, eldeki stoğa dokunmaz (05 §8: 10 / 3 → kullanılabilir 7)")
    void givenEnoughStock_whenReserve_thenReservedIncreases() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).build();

        // When
        product.reserve(new Quantity(3));

        // Then
        assertThat(product.getOnHand()).isEqualTo(new Quantity(10));
        assertThat(product.getReserved()).isEqualTo(new Quantity(3));
        assertThat(product.available()).isEqualTo(new Quantity(7));
    }

    @Test
    @DisplayName("Kullanılabilirden fazla reserve değişmezi bozar: InsufficientStockException (saga'da bu yol kullanılmaz)")
    void givenNotEnoughStock_whenReserve_thenInsufficientStock() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(5).withReserved(4).build();

        // When / Then
        assertThatThrownBy(() -> product.reserve(new Quantity(2)))
                .isInstanceOfSatisfying(InsufficientStockException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.INSUFFICIENT_STOCK));
        assertThat(product.getReserved()).isEqualTo(new Quantity(4));
    }

    @Test
    @DisplayName("Release ayrılan stoğu kullanılabilire geri verir (C2)")
    void givenReservedStock_whenRelease_thenReservedDecreases() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(3).build();

        // When
        product.release(new Quantity(3));

        // Then
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
        assertThat(product.available()).isEqualTo(new Quantity(10));
    }

    @Test
    @DisplayName("Commit ayrılan stoğu depodan düşer (05 §8: 10 / 3 → 7 / 0)")
    void givenReservedStock_whenCommit_thenOnHandAndReservedDecrease() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(3).build();

        // When
        product.commit(new Quantity(3));

        // Then
        assertThat(product.getOnHand()).isEqualTo(new Quantity(7));
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
    }

    @Test
    @DisplayName("Ayrılandan fazlası release ya da commit edilemez")
    void givenLessReserved_whenReleaseOrCommitMore_thenRejected() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(1).build();

        // When / Then
        assertThatThrownBy(() -> product.release(new Quantity(2)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.RESERVED_STOCK_INSUFFICIENT));
        assertThatThrownBy(() -> product.commit(new Quantity(2)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.RESERVED_STOCK_INSUFFICIENT));
    }

    @Test
    @DisplayName("Fiyat değişince katalog sürümü artar; optimistic lock sürümüne dokunulmaz")
    void givenNewPrice_whenChangeDetails_thenCatalogVersionIncreases() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withCatalogVersion(3L).withVersion(12L).build();
        final Money newPrice = new Money(new BigDecimal("279.90"), TRY);

        // When
        final boolean changed = product.changeDetails(ProductValidTestData.Product.NAME, newPrice);

        // Then
        assertThat(changed).isTrue();
        assertThat(product.getUnitPrice()).isEqualTo(newPrice);
        assertThat(product.getCatalogVersion()).isEqualTo(4L);
        assertThat(product.getVersion()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Aynı ad ve fiyat değişiklik sayılmaz: katalog sürümü artmaz, product.changed yayınlanmaz")
    void givenSameDetails_whenChangeDetails_thenUnchanged() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();

        // When
        final boolean changed = product.changeDetails(ProductValidTestData.Product.NAME, new Money(new BigDecimal("249.9"), TRY));

        // Then
        assertThat(changed).isFalse();
        assertThat(product.getCatalogVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Geçersiz ad reddedilir ve ürün değişmez")
    void givenBlankName_whenChangeDetails_thenRejectedAndUnchanged() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();

        // When / Then
        assertThatThrownBy(() -> product.changeDetails(" ", ProductValidTestData.Product.UNIT_PRICE_MONEY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NAME_REQUIRED));
        assertThat(product.getName()).isEqualTo(ProductValidTestData.Product.NAME);
        assertThat(product.getCatalogVersion()).isEqualTo(1L);
    }
}
