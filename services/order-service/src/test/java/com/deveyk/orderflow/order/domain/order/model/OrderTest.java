package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.InvalidOrderStateException;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.EUR;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final CustomerId CUSTOMER = new CustomerId(OrderValidTestData.Customer.ID);
    private static final Instant LATER = NOW.plusSeconds(60);

    @Test
    @DisplayName("Yeni sipariş PENDING başlar; kimliği, zamanları ve sürümü verilir, toplam satırların toplamıdır")
    void givenValidLines_whenPlace_thenPendingWithTotal() {
        // Given
        final OrderLine first = new OrderLineBuilder()
                .withValidValues()
                .withUnitPrice(new Money(new BigDecimal("100.00"), TRY))
                .withQuantity(new Quantity(2))
                .build();

        final OrderLine second = new OrderLineBuilder()
                .withValidValues()
                .withRandomProduct()
                .withUnitPrice(new Money(new BigDecimal("49.90"), TRY))
                .build();

        // When
        final Order order = Order.place(CUSTOMER, List.of(first, second), NOW);

        // Then
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.isPending()).isTrue();
        assertThat(order.getPlacedAt()).isEqualTo(NOW);
        assertThat(order.getLastModifiedAt()).isEqualTo(NOW);
        assertThat(order.getVersion()).isZero();
        assertThat(order.getCancellationReason()).isEmpty();
        assertThat(order.getTotal()).isEqualTo(new Money(new BigDecimal("249.90"), TRY));
    }

    @Test
    @DisplayName("Yeni sipariş en fazla MAX_LINES satır içerebilir")
    void givenTooManyLines_whenPlace_thenRejected() {
        // Given
        final List<OrderLine> lines = linesWithDistinctProducts(Order.MAX_LINES + 1);

        // When / Then
        assertThatThrownBy(() -> Order.place(CUSTOMER, lines, NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.TOO_MANY_LINES));
    }

    @Test
    @DisplayName("Satır limiti bir politikadır: limitten fazla satırlı kayıtlı sipariş yine de rehydrate edilir")
    void givenStoredOrderAboveLimit_whenRehydrate_thenAccepted() {
        // Given
        final List<OrderLine> lines = linesWithDistinctProducts(Order.MAX_LINES + 1);

        // When
        final Order order = new OrderBuilder()
                .withValidValues()
                .withLines(lines)
                .build();

        // Then
        assertThat(order.getLines()).hasSize(Order.MAX_LINES + 1);
    }

    @Test
    @DisplayName("Satırı olmayan sipariş verilemez")
    void givenNoLines_whenPlace_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> Order.place(CUSTOMER, List.of(), NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_LINES_REQUIRED));
    }

    @Test
    @DisplayName("Müşterisi olmayan sipariş verilemez")
    void givenNoCustomer_whenPlace_thenRejected() {
        // Given
        final List<OrderLine> lines = List.of(new OrderLineBuilder().withValidValues().build());

        // When / Then
        assertThatThrownBy(() -> Order.place(null, lines, NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.CUSTOMER_REQUIRED));
    }

    @Test
    @DisplayName("Satırları farklı para birimlerinde olan sipariş verilemez")
    void givenMixedCurrencies_whenPlace_thenRejected() {
        // Given
        final OrderLine lira = new OrderLineBuilder()
                .withValidValues()
                .build();

        final OrderLine euro = new OrderLineBuilder()
                .withValidValues()
                .withRandomProduct()
                .withUnitPrice(new Money(BigDecimal.TEN, EUR))
                .build();

        // When / Then
        assertThatThrownBy(() -> Order.place(CUSTOMER, List.of(lira, euro), NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.MIXED_CURRENCY));
    }

    @Test
    @DisplayName("Aynı ürün iki satırda olamaz; satırlar birleştirilmez, sipariş reddedilir")
    void givenSameProductTwice_whenPlace_thenRejected() {
        // Given
        final OrderLine first = new OrderLineBuilder()
                .withValidValues()
                .build();

        final OrderLine second = new OrderLineBuilder()
                .withValidValues()
                .withQuantity(new Quantity(3))
                .build();

        // When / Then
        assertThatThrownBy(() -> Order.place(CUSTOMER, List.of(first, second), NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.DUPLICATE_PRODUCT_LINE));
    }

    @Test
    @DisplayName("Sipariş verildikten sonra satır listesi değiştirilemez")
    void givenPlacedOrder_whenModifyLines_thenUnsupported() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        final OrderLine extra = new OrderLineBuilder()
                .withValidValues()
                .withRandomProduct()
                .build();

        // When / Then
        assertThatThrownBy(() -> order.getLines().add(extra)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("CANCELLED bir kayıt iptal sebebi olmadan rehydrate edilemez")
    void givenCancelledWithoutReason_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderBuilder().withValidValues().withStatus(OrderStatus.CANCELLED).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.CANCELLATION_REASON_REQUIRED));
    }

    @Test
    @DisplayName("CANCELLED olmayan bir kayıt iptal sebebi taşıyamaz")
    void givenPendingWithReason_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new OrderBuilder().withValidValues().withCancellationReason(CancellationReason.PAYMENT_DECLINED).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.CANCELLATION_REASON_NOT_ALLOWED));
    }

    @Test
    @DisplayName("PENDING sipariş tamamlanır ve değişiklik zamanı güncellenir")
    void givenPendingOrder_whenComplete_thenCompleted() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When
        order.complete(LATER);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getLastModifiedAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("Zaten COMPLETED olan sipariş tekrar tamamlanınca hiçbir şey değişmez")
    void givenCompletedOrder_whenCompleteAgain_thenNoChange() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withStatus(OrderStatus.COMPLETED)
                .build();

        // When
        order.complete(LATER);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getLastModifiedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("CANCELLED sipariş tamamlanamaz")
    void givenCancelledOrder_whenComplete_thenInvalidState() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withStatus(OrderStatus.CANCELLED)
                .withCancellationReason(CancellationReason.SAGA_TIMEOUT)
                .build();

        // When / Then
        assertThatThrownBy(() -> order.complete(LATER))
                .isInstanceOfSatisfying(InvalidOrderStateException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.INVALID_ORDER_STATE));
    }

    @Test
    @DisplayName("PENDING sipariş sebebiyle iptal edilir")
    void givenPendingOrder_whenCancel_thenCancelledWithReason() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When
        order.cancel(CancellationReason.INSUFFICIENT_STOCK, LATER);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).contains(CancellationReason.INSUFFICIENT_STOCK);
        assertThat(order.getLastModifiedAt()).isEqualTo(LATER);
    }

    @Test
    @DisplayName("Zaten CANCELLED olan sipariş tekrar iptal edilince ilk sebep korunur")
    void givenCancelledOrder_whenCancelAgain_thenFirstReasonKept() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withStatus(OrderStatus.CANCELLED)
                .withCancellationReason(CancellationReason.INSUFFICIENT_STOCK)
                .build();

        // When
        order.cancel(CancellationReason.SAGA_TIMEOUT, LATER);

        // Then
        assertThat(order.getCancellationReason()).contains(CancellationReason.INSUFFICIENT_STOCK);
        assertThat(order.getLastModifiedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("COMPLETED sipariş iptal edilemez")
    void givenCompletedOrder_whenCancel_thenInvalidState() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withStatus(OrderStatus.COMPLETED)
                .build();

        // When / Then
        assertThatThrownBy(() -> order.cancel(CancellationReason.PAYMENT_DECLINED, LATER))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("İptal sebebi ve zaman zorunludur")
    void givenMissingReasonOrTime_whenCancel_thenRejected() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When / Then
        assertThatThrownBy(() -> order.cancel(null, LATER))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.CANCELLATION_REASON_REQUIRED));

        assertThatThrownBy(() -> order.cancel(CancellationReason.PAYMENT_DECLINED, null))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.TIMESTAMP_REQUIRED));

        assertThat(order.isPending()).isTrue();
    }

    @Test
    @DisplayName("Siparişler kimlikleriyle karşılaştırılır")
    void givenSameIdDifferentState_whenEquals_thenEqual() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        final Order sameIdCompleted = Order.rehydrate()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .lines(order.getLines())
                .placedAt(order.getPlacedAt())
                .status(OrderStatus.COMPLETED)
                .lastModifiedAt(LATER)
                .version(3L)
                .build();

        // Then
        assertThat(sameIdCompleted).isEqualTo(order).hasSameHashCodeAs(order);
    }

    private static List<OrderLine> linesWithDistinctProducts(final int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new OrderLineBuilder().withValidValues().withRandomProduct().build())
                .toList();
    }
}
