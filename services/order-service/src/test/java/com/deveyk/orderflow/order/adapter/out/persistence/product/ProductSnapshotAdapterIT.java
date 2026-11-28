package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ProductSnapshotBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class ProductSnapshotAdapterIT extends OrderTestContainerConfiguration {

    private static final Money NEW_PRICE = new Money(new BigDecimal("279.90"), TRY);

    @Autowired
    private ProductSnapshotAdapter productSnapshotAdapter;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Seed'deki ürün kopyaları adı, fiyatı ve para birimiyle okunur; bilinmeyen id sonuçta yer almaz")
    void givenSeededAndUnknownIds_whenFindAllByIds_thenOnlySeededReturned() {
        // Given
        final ProductId headphones = OrderValidTestData.Product.PRODUCT_ID;
        final ProductId cable = OrderValidTestData.SecondProduct.PRODUCT_ID;
        final ProductId unknown = new ProductId(UUID.randomUUID());

        // When
        final List<ProductSnapshot> found = productSnapshotAdapter.findAllByIds(List.of(headphones, cable, unknown));

        // Then
        assertThat(found)
                .extracting(ProductSnapshot::getId, ProductSnapshot::getName, ProductSnapshot::getUnitPrice)
                .containsExactlyInAnyOrder(
                        tuple(headphones, OrderValidTestData.Product.NAME, OrderValidTestData.Product.UNIT_PRICE_MONEY),
                        tuple(cable, OrderValidTestData.SecondProduct.NAME, OrderValidTestData.SecondProduct.UNIT_PRICE_MONEY)
                );
    }

    @Test
    @DisplayName("Boş id listesi için boş liste döner")
    void givenNoIds_whenFindAllByIds_thenEmpty() {
        // When / Then
        assertThat(productSnapshotAdapter.findAllByIds(List.of())).isEmpty();
    }

    @Test
    @DisplayName("Yeni kopya kaydedilir ve ürün sürümüyle birlikte okunur")
    void givenNewSnapshot_whenCreate_thenReadBackWithProductVersion() {
        // Given
        final ProductSnapshot created = new ProductSnapshotBuilder()
                .withValidValues()
                .withRandomProduct()
                .withName("Studio Monitor")
                .withUnitPrice(NEW_PRICE)
                .withProductVersion(2L)
                .build();

        // When
        productSnapshotAdapter.create(created);

        // Then
        final ProductSnapshot found = productSnapshotAdapter.findById(created.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Studio Monitor");
        assertThat(found.getUnitPrice()).isEqualTo(NEW_PRICE);
        assertThat(found.getProductVersion()).isEqualTo(2L);
        assertThat(found.getVersion()).isZero();
    }

    @Test
    @DisplayName("Okunup değiştirilen kopya güncellenir; satırın optimistic lock sürümü artar")
    void givenReadAndChangedSnapshot_whenUpdate_thenStateAndVersionUpdated() {
        // Given
        final ProductId productId = createSnapshot(1L);

        // When
        transactionTemplate.executeWithoutResult(status -> {
            final ProductSnapshot product = productSnapshotAdapter.findById(productId).orElseThrow();
            product.applyChange("Studio Monitor II", NEW_PRICE, 2L);
            productSnapshotAdapter.update(product);
        });

        // Then
        final ProductSnapshot found = productSnapshotAdapter.findById(productId).orElseThrow();
        assertThat(found.getName()).isEqualTo("Studio Monitor II");
        assertThat(found.getUnitPrice()).isEqualTo(NEW_PRICE);
        assertThat(found.getProductVersion()).isEqualTo(2L);
        assertThat(found.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Okunduktan sonra başkası güncellediyse OptimisticLockingFailureException; değişiklik yazılmaz")
    void givenConcurrentUpdate_whenUpdateStaleCopy_thenOptimisticLockFailure() {
        // Given
        final ProductId productId = createSnapshot(1L);
        final ProductSnapshot stale = productSnapshotAdapter.findById(productId).orElseThrow();
        transactionTemplate.executeWithoutResult(status -> {
            final ProductSnapshot current = productSnapshotAdapter.findById(productId).orElseThrow();
            current.applyChange("Studio Monitor II", NEW_PRICE, 2L);
            productSnapshotAdapter.update(current);
        });
        stale.applyChange("Studio Monitor III", NEW_PRICE, 3L);

        // When / Then
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> productSnapshotAdapter.update(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(productSnapshotAdapter.findById(productId).orElseThrow().getProductVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Transaction dışında güncelleme reddedilir (MANDATORY): değişiklik sessizce kaybolmasın")
    void givenNoTransaction_whenUpdate_thenIllegalTransactionState() {
        // Given
        final ProductSnapshot product = productSnapshotAdapter.findById(createSnapshot(1L)).orElseThrow();

        // When / Then
        assertThatThrownBy(() -> productSnapshotAdapter.update(product))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    private ProductId createSnapshot(final long productVersion) {
        final ProductSnapshot created = new ProductSnapshotBuilder()
                .withValidValues()
                .withRandomProduct()
                .withName("Studio Monitor")
                .withProductVersion(productVersion)
                .build();
        productSnapshotAdapter.create(created);
        return created.getId();
    }
}
