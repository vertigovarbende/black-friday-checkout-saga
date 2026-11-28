package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.exception.InvalidReservationStateException;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.LATER;
import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.NOW;
import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockReservationTest {

    private static final OrderId ORDER_ID = ProductValidTestData.Order.ORDER_ID;
    private static final ProductId PRODUCT_ID = ProductValidTestData.Product.PRODUCT_ID;
    private static final ProductId SECOND_PRODUCT_ID = ProductValidTestData.SecondProduct.PRODUCT_ID;
    private static final Money PRICE = ProductValidTestData.Product.UNIT_PRICE_MONEY;
    private static final Money SECOND_PRICE = ProductValidTestData.SecondProduct.UNIT_PRICE_MONEY;

    // ---------------------------------------------------------------- request (saga T2)

    @Test
    @DisplayName("Stok ve fiyat uygunsa RESERVED; her satırın ürünü kadar stok ayrılır")
    void givenEnoughStockAndCurrentPrices_whenRequest_thenReservedAndStockHeld() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).build();
        final Product second = new ProductBuilder().withValidValues().withSecondProduct().build();

        // When
        final StockReservation reservation = StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(PRODUCT_ID, new Quantity(3), PRICE), new RequestedLine(SECOND_PRODUCT_ID, new Quantity(1), SECOND_PRICE)),
                Map.of(PRODUCT_ID, product, SECOND_PRODUCT_ID, second), NOW);

        // Then
        assertThat(reservation.getId()).isEqualTo(ORDER_ID);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getRejection()).isEmpty();
        assertThat(reservation.getLines()).containsExactly(new ReservationLine(PRODUCT_ID, new Quantity(3)), new ReservationLine(SECOND_PRODUCT_ID, new Quantity(1)));
        assertThat(reservation.getCreatedAt()).isEqualTo(NOW);
        assertThat(product.getReserved()).isEqualTo(new Quantity(3));
        assertThat(second.getReserved()).isEqualTo(new Quantity(1));
    }

    @Test
    @DisplayName("Stok yetmezse exception değil REJECTED (INSUFFICIENT_STOCK) ve hiçbir ürüne dokunulmaz")
    void givenNotEnoughStockOnOneLine_whenRequest_thenRejectedAndNothingHeld() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).build();
        final Product second = new ProductBuilder().withValidValues().withSecondProduct().withOnHand(1).build();

        // When
        final StockReservation reservation = StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(PRODUCT_ID, new Quantity(3), PRICE), new RequestedLine(SECOND_PRODUCT_ID, new Quantity(2), SECOND_PRICE)),
                Map.of(PRODUCT_ID, product, SECOND_PRODUCT_ID, second), NOW);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(reservation.getRejection()).contains(new StockRejection(RejectionReason.INSUFFICIENT_STOCK, SECOND_PRODUCT_ID));
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
        assertThat(second.getReserved()).isEqualTo(Quantity.ZERO);
    }

    @Test
    @DisplayName("İstekteki fiyat ürünün güncel fiyatı değilse PRICE_CHANGED (fiyatın kesin kontrolü, 02 §3.1)")
    void givenStalePrice_whenRequest_thenPriceChanged() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();
        final Money stalePrice = new Money(new BigDecimal("199.90"), TRY);

        // When
        final StockReservation reservation = StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(PRODUCT_ID, new Quantity(1), stalePrice)), Map.of(PRODUCT_ID, product), NOW);

        // Then
        assertThat(reservation.getRejection()).contains(new StockRejection(RejectionReason.PRICE_CHANGED, PRODUCT_ID));
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
    }

    @Test
    @DisplayName("product-service'in tanımadığı ürün UNKNOWN_PRODUCT")
    void givenUnknownProduct_whenRequest_thenUnknownProduct() {
        // Given
        final ProductId unknown = new ProductId(UUID.randomUUID());

        // When
        final StockReservation reservation = StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(unknown, new Quantity(1), PRICE)), Map.of(), NOW);

        // Then
        assertThat(reservation.getRejection()).contains(new StockRejection(RejectionReason.UNKNOWN_PRODUCT, unknown));
    }

    @Test
    @DisplayName("Sebeplerin önceliği: bilinmeyen ürün, sonra fiyat, sonra stok; ilk satırdaki stok sorunu ikinci satırdaki fiyat sorununun önüne geçmez")
    void givenStockProblemThenPriceProblem_whenRequest_thenPriceChangedFirst() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(1).build();
        final Product second = new ProductBuilder().withValidValues().withSecondProduct().build();
        final Money stalePrice = new Money(new BigDecimal("9.99"), TRY);

        // When
        final StockReservation reservation = StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(PRODUCT_ID, new Quantity(5), PRICE), new RequestedLine(SECOND_PRODUCT_ID, new Quantity(1), stalePrice)),
                Map.of(PRODUCT_ID, product, SECOND_PRODUCT_ID, second), NOW);

        // Then
        assertThat(reservation.getRejection()).contains(new StockRejection(RejectionReason.PRICE_CHANGED, SECOND_PRODUCT_ID));
    }

    @Test
    @DisplayName("Satırsız istek ve aynı ürünün iki satırı reddedilir")
    void givenNoLinesOrDuplicateProduct_whenRequest_thenRejected() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();

        // When / Then
        assertThatThrownBy(() -> StockReservation.request(ORDER_ID, List.of(), Map.of(PRODUCT_ID, product), NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.RESERVATION_LINES_REQUIRED));
        assertThatThrownBy(() -> StockReservation.request(ORDER_ID,
                List.of(new RequestedLine(PRODUCT_ID, new Quantity(1), PRICE), new RequestedLine(PRODUCT_ID, new Quantity(2), PRICE)),
                Map.of(PRODUCT_ID, product), NOW))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.DUPLICATE_PRODUCT_LINE));
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
    }

    // ---------------------------------------------------------------- closed (kapanış kaydı)

    @Test
    @DisplayName("Kapanış kaydı satırsız RELEASED'dır")
    void givenOrderId_whenClosed_thenReleasedWithoutLines() {
        // When
        final StockReservation reservation = StockReservation.closed(ORDER_ID, NOW);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(reservation.isClosingRecord()).isTrue();
        assertThat(reservation.productIds()).isEmpty();
    }

    // ---------------------------------------------------------------- release (C2)

    @Test
    @DisplayName("RESERVED release edilince RELEASED olur ve stok kullanılabilire döner")
    void givenReserved_whenRelease_thenReleasedAndStockReturned() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(2).build();
        final StockReservation reservation = new StockReservationBuilder().withValidValues().build();

        // When
        final boolean released = reservation.release(Map.of(PRODUCT_ID, product), LATER);

        // Then
        assertThat(released).isTrue();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(reservation.getLastModifiedAt()).isEqualTo(LATER);
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ReservationStatus.class, names = {"RELEASED", "REJECTED"})
    @DisplayName("Ayrılmış stok yoksa (RELEASED, REJECTED) hedef zaten sağlanmış: release false döner, hiçbir şey değişmez")
    void givenNothingHeld_whenRelease_thenFalseAndUnchanged(final ReservationStatus status) {
        // Given
        final StockReservationBuilder builder = new StockReservationBuilder().withValidValues();
        final StockReservation reservation = status == ReservationStatus.REJECTED
                ? builder.withRejection(RejectionReason.INSUFFICIENT_STOCK).build()
                : builder.withStatus(ReservationStatus.RELEASED).build();

        // When
        final boolean released = reservation.release(Map.of(), LATER);

        // Then
        assertThat(released).isFalse();
        assertThat(reservation.getStatus()).isEqualTo(status);
        assertThat(reservation.getLastModifiedAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("COMMITTED rezervasyon release edilemez: hedefe artık gidilemez (DLT)")
    void givenCommitted_whenRelease_thenInvalidState() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.COMMITTED).build();

        // When / Then
        assertThatThrownBy(() -> reservation.release(Map.of(), LATER))
                .isInstanceOfSatisfying(InvalidReservationStateException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.INVALID_RESERVATION_STATE));
    }

    // ---------------------------------------------------------------- commit (order.completed)

    @Test
    @DisplayName("RESERVED commit edilince COMMITTED olur ve stok depodan düşer")
    void givenReserved_whenCommit_thenCommittedAndStockDeducted() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(2).build();
        final StockReservation reservation = new StockReservationBuilder().withValidValues().build();

        // When
        final boolean committed = reservation.commit(Map.of(PRODUCT_ID, product), LATER);

        // Then
        assertThat(committed).isTrue();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMMITTED);
        assertThat(product.getOnHand()).isEqualTo(new Quantity(8));
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
    }

    @Test
    @DisplayName("Zaten COMMITTED ise commit false döner (iki kez commit etkisizdir, 05 §8)")
    void givenCommitted_whenCommit_thenFalse() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.COMMITTED).build();

        // When / Then
        assertThat(reservation.commit(Map.of(), LATER)).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = ReservationStatus.class, names = {"RELEASED", "REJECTED"})
    @DisplayName("RELEASED ve REJECTED commit edilemez (DLT)")
    void givenReleasedOrRejected_whenCommit_thenInvalidState(final ReservationStatus status) {
        // Given
        final StockReservationBuilder builder = new StockReservationBuilder().withValidValues();
        final StockReservation reservation = status == ReservationStatus.REJECTED
                ? builder.withRejection(RejectionReason.INSUFFICIENT_STOCK).build()
                : builder.withStatus(ReservationStatus.RELEASED).build();

        // When / Then
        assertThatThrownBy(() -> reservation.commit(Map.of(), LATER)).isInstanceOf(InvalidReservationStateException.class);
    }

    // ---------------------------------------------------------------- tekrar gelen reserve isteğinin cevabı (checkout-saga.md §5)

    @Test
    @DisplayName("Tekrar gelen reserve isteğinin cevabı: RESERVED / COMMITTED → reserved, REJECTED → kendi sebebi, RELEASED → RESERVATION_CLOSED")
    void givenEachStatus_whenRejectionOfReserveRequest_thenReplyMatchesState() {
        // Given
        final StockReservation reserved = new StockReservationBuilder().withValidValues().build();
        final StockReservation committed = new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.COMMITTED).build();
        final StockReservation rejected = new StockReservationBuilder().withValidValues().withRejection(RejectionReason.PRICE_CHANGED).build();
        final StockReservation closed = new StockReservationBuilder().withValidValues().withClosingRecord().build();

        // When / Then
        assertThat(reserved.rejectionOfReserveRequest()).isEmpty();
        assertThat(committed.rejectionOfReserveRequest()).isEmpty();
        assertThat(rejected.rejectionOfReserveRequest()).contains(new StockRejection(RejectionReason.PRICE_CHANGED, PRODUCT_ID));
        assertThat(closed.rejectionOfReserveRequest()).contains(StockRejection.reservationClosed());
    }

    // ---------------------------------------------------------------- yapısal değişmezler

    @Test
    @DisplayName("REJECTED'ın sebebi olmalı; RESERVATION_CLOSED saklanan bir sebep değildir")
    void givenRejectedWithoutStoredReason_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.REJECTED).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.REJECTION_REASON_REQUIRED));
        assertThatThrownBy(() -> new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.REJECTED)
                .withRejection(StockRejection.reservationClosed()).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.REJECTION_REASON_REQUIRED));
    }

    @Test
    @DisplayName("REJECTED olmayan rezervasyonun ret sebebi olamaz")
    void givenReservedWithRejection_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new StockReservationBuilder().withValidValues()
                .withRejection(new StockRejection(RejectionReason.INSUFFICIENT_STOCK, PRODUCT_ID)).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.REJECTION_REASON_NOT_ALLOWED));
    }

    @Test
    @DisplayName("Satırsız sadece kapanış kaydı (RELEASED) olabilir")
    void givenReservedWithoutLines_whenRehydrate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new StockReservationBuilder().withValidValues().withLines(List.of()).build())
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.RESERVATION_LINES_REQUIRED));
    }
}
