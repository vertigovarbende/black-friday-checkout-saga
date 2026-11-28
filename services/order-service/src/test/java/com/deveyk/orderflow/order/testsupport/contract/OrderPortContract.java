package com.deveyk.orderflow.order.testsupport.contract;

import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

// OrderReadPort + OrderSavePort sözleşmesi (07 §12.1). Aynı testler fake'e (Surefire) ve gerçek adapter'a (Failsafe) karşı koşar.
public interface OrderPortContract {

    Instant LATER = NOW.plusSeconds(60);

    OrderReadPort readPort();

    OrderSavePort savePort();

    // Gerçek adapter'da update transaction ister; fake'te action doğrudan çalışır.
    void inTransaction(Runnable action);

    @Test
    @DisplayName("Olmayan sipariş için Optional.empty döner, exception fırlatmaz")
    default void givenUnknownId_whenFindById_thenEmpty() {
        // When / Then
        assertThat(readPort().findById(OrderId.newId())).isEmpty();
    }

    @Test
    @DisplayName("Yeni sipariş kaydedilir; satırları, fiyatları ve adetleriyle aynı sırada geri okunur")
    default void givenNewOrder_whenCreateAndFind_thenSameOrder() {
        // Given
        final OrderLine first = new OrderLineBuilder()
                .withValidValues()
                .withQuantity(new Quantity(2))
                .build();

        final OrderLine second = new OrderLineBuilder()
                .withValidValues()
                .withSecondProduct()
                .build();

        final Order order = new OrderBuilder()
                .withValidValues()
                .withLines(List.of(first, second))
                .build();

        // When
        inTransaction(() -> savePort().create(order));
        final Order found = readPort().findById(order.getId()).orElseThrow();

        // Then
        assertThat(found).isEqualTo(order);
        assertThat(found.getCustomerId()).isEqualTo(order.getCustomerId());
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.getCancellationReason()).isEmpty();
        assertThat(found.getPlacedAt()).isEqualTo(NOW);
        assertThat(found.getLastModifiedAt()).isEqualTo(NOW);
        assertThat(found.getVersion()).isZero();
        assertThat(found.getTotal()).isEqualTo(new Money(new BigDecimal("514.80"), TRY));
        assertThat(found.getLines())
                .extracting(OrderLine::getId, OrderLine::getProductId, OrderLine::getProductName, OrderLine::getUnitPrice, OrderLine::getQuantity)
                .containsExactly(
                        tuple(first.getId(), first.getProductId(), first.getProductName(), first.getUnitPrice(), first.getQuantity()),
                        tuple(second.getId(), second.getProductId(), second.getProductName(), second.getUnitPrice(), second.getQuantity())
                );
    }

    @Test
    @DisplayName("Okunup tamamlanan sipariş güncellenir ve sürümü bir artar")
    default void givenStoredOrder_whenCompleteAndUpdate_thenPersistedWithNextVersion() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> savePort().create(order));

        // When
        inTransaction(() -> {
            final Order loaded = readPort().findById(order.getId()).orElseThrow();
            loaded.complete(LATER);
            savePort().update(loaded);
        });

        // Then
        final Order reloaded = readPort().findById(order.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(reloaded.getLastModifiedAt()).isEqualTo(LATER);
        assertThat(reloaded.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("İptal edilen siparişin sebebi de kaydedilir")
    default void givenStoredOrder_whenCancelAndUpdate_thenReasonPersisted() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> savePort().create(order));

        // When
        inTransaction(() -> {
            final Order loaded = readPort().findById(order.getId()).orElseThrow();
            loaded.cancel(CancellationReason.PAYMENT_DECLINED, LATER);
            savePort().update(loaded);
        });

        // Then
        final Order reloaded = readPort().findById(order.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(reloaded.getCancellationReason()).contains(CancellationReason.PAYMENT_DECLINED);
    }

    @Test
    @DisplayName("Eski sürümden okunmuş sipariş güncellenemez; ilk yazan kazanır")
    default void givenStaleCopy_whenUpdate_thenOptimisticLockFailure() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        inTransaction(() -> savePort().create(order));
        final Order first = readPort().findById(order.getId()).orElseThrow();
        final Order stale = readPort().findById(order.getId()).orElseThrow();
        inTransaction(() -> {
            first.complete(LATER);
            savePort().update(first);
        });

        // When / Then
        assertThatThrownBy(() -> inTransaction(() -> {
            stale.cancel(CancellationReason.SAGA_TIMEOUT, LATER);
            savePort().update(stale);
        })).isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(readPort().findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Kayıtlı olmayan sipariş güncellenmek istenirse OrderNotFoundException fırlar")
    default void givenUnknownOrder_whenUpdate_thenNotFound() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When / Then
        assertThatThrownBy(() -> inTransaction(() -> savePort().update(order)))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
