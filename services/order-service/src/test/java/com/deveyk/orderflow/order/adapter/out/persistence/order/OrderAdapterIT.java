package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class OrderAdapterIT extends OrderTestContainerConfiguration {

    private static final Instant LATER = NOW.plusSeconds(60);

    @Autowired
    private OrderAdapter orderAdapter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("Yeni sipariş kaydedilir; satırları, fiyatları ve adetleriyle aynı sırada geri okunur")
    void givenNewOrder_whenCreateAndFind_thenSameOrder() {
        // Given
        final OrderLine first = new OrderLineBuilder()
                .withValidValues()
                .withUnitPrice(new Money(new BigDecimal("249.90"), TRY))
                .withQuantity(new Quantity(2))
                .build();

        final OrderLine second = new OrderLineBuilder()
                .withValidValues()
                .withRandomProduct()
                .withUnitPrice(new Money(new BigDecimal("15.00"), TRY))
                .build();

        final Order order = Order.place(new CustomerId(OrderValidTestData.Customer.ID), List.of(first, second), NOW);

        // When
        inTransaction(() -> orderAdapter.create(order));
        final Order found = orderAdapter.findById(order.getId()).orElseThrow();

        // Then
        assertThat(found).isEqualTo(order);
        assertThat(found.getCustomerId()).isEqualTo(order.getCustomerId());
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.getPlacedAt()).isEqualTo(NOW);
        assertThat(found.getLastModifiedAt()).isEqualTo(NOW);
        assertThat(found.getVersion()).isZero();
        assertThat(found.getTotal()).isEqualTo(new Money(new BigDecimal("514.80"), TRY));
        assertThat(found.getLines())
                .extracting(OrderLine::getId, OrderLine::getProductId, OrderLine::getUnitPrice, OrderLine::getQuantity)
                .containsExactly(
                        tuple(first.getId(), first.getProductId(), first.getUnitPrice(), first.getQuantity()),
                        tuple(second.getId(), second.getProductId(), second.getUnitPrice(), second.getQuantity())
                );
    }

    @Test
    @DisplayName("Olmayan sipariş için Optional.empty döner")
    void givenUnknownId_whenFindById_thenEmpty() {
        // When / Then
        assertThat(orderAdapter.findById(OrderId.newId())).isEmpty();
    }

    @Test
    @DisplayName("Okunup tamamlanan sipariş güncellenir ve sürümü bir artar")
    void givenStoredOrder_whenCompleteAndUpdate_thenPersistedWithNextVersion() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> orderAdapter.create(order));

        // When
        inTransaction(() -> {
            final Order loaded = orderAdapter.findById(order.getId()).orElseThrow();
            loaded.complete(LATER);
            orderAdapter.update(loaded);
        });

        // Then
        final Order reloaded = orderAdapter.findById(order.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(reloaded.getLastModifiedAt()).isEqualTo(LATER);
        assertThat(reloaded.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("İptal edilen siparişin sebebi de kaydedilir")
    void givenStoredOrder_whenCancelAndUpdate_thenReasonPersisted() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> orderAdapter.create(order));

        // When
        inTransaction(() -> {
            final Order loaded = orderAdapter.findById(order.getId()).orElseThrow();
            loaded.cancel(CancellationReason.PAYMENT_DECLINED, LATER);
            orderAdapter.update(loaded);
        });

        // Then
        final Order reloaded = orderAdapter.findById(order.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(reloaded.getCancellationReason()).contains(CancellationReason.PAYMENT_DECLINED);
    }

    @Test
    @DisplayName("Eski sürümden okunmuş sipariş güncellenemez; ilk yazan kazanır")
    void givenStaleCopy_whenUpdate_thenOptimisticLockFailure() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> orderAdapter.create(order));
        final Order first = orderAdapter.findById(order.getId()).orElseThrow();
        final Order stale = orderAdapter.findById(order.getId()).orElseThrow();
        inTransaction(() -> {
            first.complete(LATER);
            orderAdapter.update(first);
        });

        // When / Then
        assertThatThrownBy(() -> inTransaction(() -> {
            stale.cancel(CancellationReason.SAGA_TIMEOUT, LATER);
            orderAdapter.update(stale);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(orderAdapter.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Kayıtlı olmayan sipariş güncellenmek istenirse OrderNotFoundException fırlar")
    void givenUnknownOrder_whenUpdate_thenNotFound() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When / Then
        assertThatThrownBy(() -> inTransaction(() -> orderAdapter.update(order)))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("update transaction dışında çağrılamaz; değişiklik sessizce kaybolmasın")
    void givenNoTransaction_whenUpdate_thenRejected() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When / Then
        assertThatThrownBy(() -> orderAdapter.update(order)).isInstanceOf(IllegalTransactionStateException.class);
    }

    private void inTransaction(final Runnable action) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> action.run());
    }
}
