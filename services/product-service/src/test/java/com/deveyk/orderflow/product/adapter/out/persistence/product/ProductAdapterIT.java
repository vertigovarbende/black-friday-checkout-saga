package com.deveyk.orderflow.product.adapter.out.persistence.product;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ProductAdapterIT extends ProductTestContainerConfiguration {

    private static final ProductId PRODUCT_ID = ProductValidTestData.Product.PRODUCT_ID;
    private static final ProductId SECOND_PRODUCT_ID = ProductValidTestData.SecondProduct.PRODUCT_ID;

    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Ürünler ad, fiyat, stok ve katalog sürümüyle okunur; bilinmeyen id sonuçta yer almaz")
    void givenKnownAndUnknownIds_whenFindAllByIds_thenOnlyKnownReturned() {
        // When
        final List<Product> found = productAdapter.findAllByIds(List.of(PRODUCT_ID, SECOND_PRODUCT_ID, new ProductId(UUID.randomUUID())));

        // Then
        assertThat(found)
                .extracting(Product::getId, Product::getName, Product::getUnitPrice, Product::getOnHand, Product::getReserved, Product::getCatalogVersion)
                .containsExactlyInAnyOrder(
                        tuple(PRODUCT_ID, ProductValidTestData.Product.NAME, ProductValidTestData.Product.UNIT_PRICE_MONEY,
                                ProductValidTestData.Product.ON_HAND, Quantity.ZERO, 1L),
                        tuple(SECOND_PRODUCT_ID, ProductValidTestData.SecondProduct.NAME, ProductValidTestData.SecondProduct.UNIT_PRICE_MONEY,
                                ProductValidTestData.SecondProduct.ON_HAND, Quantity.ZERO, 1L));
    }

    @Test
    @DisplayName("Bütün ürünler ada göre sıralı okunur")
    void givenProducts_whenFindAll_thenSortedByName() {
        // When / Then
        assertThat(productAdapter.findAll()).extracting(Product::getName)
                .containsExactly(ProductValidTestData.SecondProduct.NAME, ProductValidTestData.Product.NAME);
    }

    @Test
    @DisplayName("Okunup değiştirilen ürün güncellenir: stok ve katalog bilgisi yazılır, optimistic lock sürümü artar")
    void givenReadAndChangedProduct_whenUpdate_thenStateAndVersionUpdated() {
        // Given
        final Money newPrice = new Money(new BigDecimal("279.90"), TRY);

        // When
        transactionTemplate.executeWithoutResult(status -> {
            final Product product = productAdapter.findById(PRODUCT_ID).orElseThrow();
            product.reserve(new Quantity(3));
            product.changeDetails("Wireless Headphones Pro", newPrice);
            productAdapter.update(product);
        });

        // Then
        final Product found = productAdapter.findById(PRODUCT_ID).orElseThrow();
        assertThat(found.getReserved()).isEqualTo(new Quantity(3));
        assertThat(found.getName()).isEqualTo("Wireless Headphones Pro");
        assertThat(found.getUnitPrice()).isEqualTo(newPrice);
        assertThat(found.getCatalogVersion()).isEqualTo(2L);
        assertThat(found.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Değişmemiş ürün için UPDATE yazılmaz: optimistic lock sürümü artmaz")
    void givenUnchangedProduct_whenUpdate_thenVersionUnchanged() {
        // When
        transactionTemplate.executeWithoutResult(status -> productAdapter.update(productAdapter.findById(PRODUCT_ID).orElseThrow()));

        // Then
        assertThat(productAdapter.findById(PRODUCT_ID).orElseThrow().getVersion()).isZero();
    }

    @Test
    @DisplayName("Okunduktan sonra başkası güncellediyse OptimisticLockingFailureException; değişiklik yazılmaz")
    void givenConcurrentUpdate_whenUpdateStaleCopy_thenOptimisticLockFailure() {
        // Given
        final Product stale = productAdapter.findById(PRODUCT_ID).orElseThrow();
        transactionTemplate.executeWithoutResult(status -> {
            final Product current = productAdapter.findById(PRODUCT_ID).orElseThrow();
            current.reserve(new Quantity(1));
            productAdapter.update(current);
        });
        stale.reserve(new Quantity(5));

        // When / Then
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> productAdapter.update(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(productAdapter.findById(PRODUCT_ID).orElseThrow().getReserved()).isEqualTo(new Quantity(1));
    }

    @Test
    @DisplayName("DB'nin CHECK'i ikinci güvenlik ağıdır: ayrılmış stok eldekini aşamaz")
    void givenReservedAboveOnHand_whenWriteDirectly_thenRejectedByCheckConstraint() {
        // When / Then
        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE products SET reserved = on_hand + 1 WHERE id = ?", PRODUCT_ID.value()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
