package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class ProductSnapshotAdapterIT extends OrderTestContainerConfiguration {

    @Autowired
    private ProductSnapshotAdapter productSnapshotAdapter;

    @Test
    @DisplayName("Seed'deki ürün kopyaları adı, fiyatı ve para birimiyle okunur; bilinmeyen id sonuçta yer almaz")
    void givenSeededAndUnknownIds_whenFindAllByIds_thenOnlySeededReturned() {
        // Given
        final ProductId headphones = new ProductId(OrderValidTestData.Product.ID);
        final ProductId cable = new ProductId(OrderValidTestData.SecondProduct.ID);
        final ProductId unknown = new ProductId(UUID.randomUUID());

        // When
        final List<ProductSnapshot> found = productSnapshotAdapter.findAllByIds(List.of(headphones, cable, unknown));

        // Then
        assertThat(found)
                .extracting(ProductSnapshot::getId, ProductSnapshot::getName, ProductSnapshot::getUnitPrice)
                .containsExactlyInAnyOrder(
                        tuple(headphones, OrderValidTestData.Product.NAME, new Money(OrderValidTestData.Product.UNIT_PRICE, TRY)),
                        tuple(cable, OrderValidTestData.SecondProduct.NAME, new Money(OrderValidTestData.SecondProduct.UNIT_PRICE, TRY))
                );
    }

    @Test
    @DisplayName("Boş id listesi için boş liste döner")
    void givenNoIds_whenFindAllByIds_thenEmpty() {
        // When / Then
        assertThat(productSnapshotAdapter.findAllByIds(List.of())).isEmpty();
    }
}
