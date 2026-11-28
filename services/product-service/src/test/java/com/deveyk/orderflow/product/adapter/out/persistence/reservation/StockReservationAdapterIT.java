package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationLine;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductTestContainerConfiguration;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.LATER;
import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockReservationAdapterIT extends ProductTestContainerConfiguration {

    @Autowired
    private StockReservationAdapter stockReservationAdapter;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("RESERVED rezervasyon satırları ve zamanlarıyla kaydedilip okunur")
    void givenReserved_whenCreate_thenReadBackWithLines() {
        // Given
        final StockReservation reservation = new StockReservationBuilder()
                .withValidValues()
                .withId(randomOrderId())
                .withLine(ProductValidTestData.SecondProduct.PRODUCT_ID, 1)
                .build();

        // When
        stockReservationAdapter.create(reservation);

        // Then
        final StockReservation found = stockReservationAdapter.findById(reservation.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(found.getLines()).containsExactlyInAnyOrder(
                new ReservationLine(ProductValidTestData.Product.PRODUCT_ID, ProductValidTestData.Order.QUANTITY),
                new ReservationLine(ProductValidTestData.SecondProduct.PRODUCT_ID, new Quantity(1)));
        assertThat(found.getRejection()).isEmpty();
        assertThat(found.getCreatedAt()).isEqualTo(NOW);
        assertThat(found.getVersion()).isZero();
    }

    @Test
    @DisplayName("REJECTED rezervasyon ret sebebi ve ürünüyle okunur")
    void givenRejected_whenCreate_thenReadBackWithRejection() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(randomOrderId())
                .withRejection(RejectionReason.PRICE_CHANGED).build();

        // When
        stockReservationAdapter.create(reservation);

        // Then
        assertThat(stockReservationAdapter.findById(reservation.getId()).orElseThrow().getRejection())
                .contains(new StockRejection(RejectionReason.PRICE_CHANGED, ProductValidTestData.Product.PRODUCT_ID));
    }

    @Test
    @DisplayName("Kapanış kaydı satırsız RELEASED olarak okunur")
    void givenClosingRecord_whenCreate_thenReadBackWithoutLines() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withId(randomOrderId()).withClosingRecord().build();

        // When
        stockReservationAdapter.create(reservation);

        // Then
        final StockReservation found = stockReservationAdapter.findById(reservation.getId()).orElseThrow();
        assertThat(found.isClosingRecord()).isTrue();
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.RELEASED);
    }

    @Test
    @DisplayName("Aynı sipariş için ikinci rezervasyon oluşturulamaz (PK = orderId)")
    void givenExistingReservation_whenCreateAgain_thenDataIntegrityViolation() {
        // Given
        final OrderId orderId = randomOrderId();
        stockReservationAdapter.create(new StockReservationBuilder().withValidValues().withId(orderId).build());

        // When / Then
        assertThatThrownBy(() -> stockReservationAdapter.create(new StockReservationBuilder().withValidValues().withId(orderId).withClosingRecord().build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Okunup release edilen rezervasyon güncellenir; satırlar değişmez, sürüm artar")
    void givenReadAndReleased_whenUpdate_thenStatusAndVersionUpdated() {
        // Given
        final OrderId orderId = randomOrderId();
        stockReservationAdapter.create(new StockReservationBuilder().withValidValues().withId(orderId).build());

        // When
        transactionTemplate.executeWithoutResult(status -> {
            final StockReservation reservation = stockReservationAdapter.findById(orderId).orElseThrow();
            reservation.release(Map.of(ProductValidTestData.Product.PRODUCT_ID,
                    new ProductBuilder().withValidValues().withReserved(2).build()), LATER);
            stockReservationAdapter.update(reservation);
        });

        // Then
        final StockReservation found = stockReservationAdapter.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(found.getLastModifiedAt()).isEqualTo(LATER);
        assertThat(found.getLines()).hasSize(1);
        assertThat(found.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Okunduktan sonra başkası güncellediyse OptimisticLockingFailureException")
    void givenConcurrentUpdate_whenUpdateStaleCopy_thenOptimisticLockFailure() {
        // Given
        final OrderId orderId = randomOrderId();
        stockReservationAdapter.create(new StockReservationBuilder().withValidValues().withId(orderId).build());
        final StockReservation stale = stockReservationAdapter.findById(orderId).orElseThrow();
        transactionTemplate.executeWithoutResult(status -> {
            final StockReservation current = stockReservationAdapter.findById(orderId).orElseThrow();
            current.commit(Map.of(ProductValidTestData.Product.PRODUCT_ID,
                    new ProductBuilder().withValidValues().withReserved(2).build()), LATER);
            stockReservationAdapter.update(current);
        });

        // When / Then
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> stockReservationAdapter.update(stale)))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    private static OrderId randomOrderId() {
        return new OrderId(UUID.randomUUID());
    }
}
