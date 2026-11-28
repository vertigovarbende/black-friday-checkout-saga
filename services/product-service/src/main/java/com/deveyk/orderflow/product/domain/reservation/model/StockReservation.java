package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.exception.InvalidReservationStateException;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.shared.domain.AggregateRoot;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// Bir siparişin stok rezervasyonu (05 §8); kimliği siparişinkidir. Stok yetmemesi exception değil REJECTED durumudur.
// Satırsız RELEASED kayıt kapanış kaydıdır: reserve'den önce gelen release'in izi (checkout-saga.md §6).
@Getter
public class StockReservation extends AggregateRoot<OrderId> {

    private final OrderId id;
    private final List<ReservationLine> lines;
    private ReservationStatus status;
    @Getter(AccessLevel.NONE)
    private final StockRejection rejection;     // sadece REJECTED iken dolu
    private final Instant createdAt;
    private Instant lastModifiedAt;

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private StockReservation(final OrderId id,
                             final List<ReservationLine> lines,
                             final ReservationStatus status,
                             final StockRejection rejection,
                             final Instant createdAt,
                             final Instant lastModifiedAt,
                             final long version) {

        if (id == null) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_ID_REQUIRED);
        }

        if (status == null) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_STATUS_REQUIRED);
        }

        if (lines == null || lines.stream().anyMatch(Objects::isNull) || (lines.isEmpty() && status != ReservationStatus.RELEASED)) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_LINES_REQUIRED, status.name());
        }

        final Optional<ProductId> duplicate = findDuplicateProduct(lines);
        if (duplicate.isPresent()) {
            throw new DomainValidationException(StockReservationErrorCode.DUPLICATE_PRODUCT_LINE, duplicate.get().value().toString());
        }

        // RESERVATION_CLOSED saklanan bir sebep değildir; kapanmış rezervasyonun durumu RELEASED'dır.
        if (status == ReservationStatus.REJECTED && (rejection == null || rejection.reason() == RejectionReason.RESERVATION_CLOSED)) {
            throw new DomainValidationException(StockReservationErrorCode.REJECTION_REASON_REQUIRED);
        }

        if (status != ReservationStatus.REJECTED && rejection != null) {
            throw new DomainValidationException(StockReservationErrorCode.REJECTION_REASON_NOT_ALLOWED, status.name());
        }

        if (createdAt == null || lastModifiedAt == null) {
            throw new DomainValidationException(StockReservationErrorCode.TIMESTAMP_REQUIRED);
        }

        this.id = id;
        this.lines = List.copyOf(lines);
        this.status = status;
        this.rejection = rejection;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        restoreVersion(version);
    }

    // Saga T2. Siparişin bütün satırlarına bakar; ilk takılan kural sebep olur: önce bilinmeyen ürün, sonra fiyat,
    // sonra stok (veri sorunları stoktan önce görünsün). Ret durumunda hiçbir ürüne dokunulmaz.
    public static StockReservation request(final OrderId orderId,
                                           final List<RequestedLine> requestedLines,
                                           final Map<ProductId, Product> products,
                                           final Instant now) {

        if (requestedLines == null || requestedLines.isEmpty() || requestedLines.stream().anyMatch(Objects::isNull)) {
            throw new DomainValidationException(StockReservationErrorCode.RESERVATION_LINES_REQUIRED, ReservationStatus.RESERVED.name());
        }

        final List<ReservationLine> lines = requestedLines.stream().map(RequestedLine::toReservationLine).toList();
        final Optional<StockRejection> rejection = firstRejection(requestedLines, products);

        if (rejection.isPresent()) {
            return new StockReservation(orderId, lines, ReservationStatus.REJECTED, rejection.get(), now, now, 0L);
        }

        final StockReservation reservation = new StockReservation(orderId, lines, ReservationStatus.RESERVED, null, now, now, 0L);
        reservation.lines.forEach(line -> products.get(line.productId()).reserve(line.quantity()));
        return reservation;
    }

    // Reserve'den önce gelen release (saga T2'de timeout): satırsız RELEASED kayıt. Sonra gelen reserve stoğa dokunmaz.
    public static StockReservation closed(final OrderId orderId, final Instant now) {
        return new StockReservation(orderId, List.of(), ReservationStatus.RELEASED, null, now, now, 0L);
    }

    // Telafi C2. Ayrılmış stok yoksa (RELEASED, REJECTED) hedef zaten sağlanmıştır: false. COMMITTED geri alınamaz.
    public boolean release(final Map<ProductId, Product> products, final Instant now) {

        if (status == ReservationStatus.RELEASED || status == ReservationStatus.REJECTED) {
            return false;
        }

        transitionTo(ReservationStatus.RELEASED, now);
        lines.forEach(line -> products.get(line.productId()).release(line.quantity()));
        return true;
    }

    // order.completed. Zaten COMMITTED ise false; RELEASED ve REJECTED commit edilemez.
    public boolean commit(final Map<ProductId, Product> products, final Instant now) {

        if (status == ReservationStatus.COMMITTED) {
            return false;
        }

        transitionTo(ReservationStatus.COMMITTED, now);
        lines.forEach(line -> products.get(line.productId()).commit(line.quantity()));
        return true;
    }

    // Bu rezervasyon için gelen bir reserve isteğinin cevabı: REJECTED ise kendi sebebi, RELEASED ise
    // RESERVATION_CLOSED (kapanmış), RESERVED / COMMITTED ise boş (stok ayrılmış).
    public Optional<StockRejection> rejectionOfReserveRequest() {
        return switch (status) {
            case RESERVED, COMMITTED -> Optional.empty();
            case REJECTED -> Optional.of(rejection);
            case RELEASED -> Optional.of(StockRejection.reservationClosed());
        };
    }

    public Optional<StockRejection> getRejection() {
        return Optional.ofNullable(rejection);
    }

    public boolean isClosingRecord() {
        return lines.isEmpty();
    }

    public Set<ProductId> productIds() {
        return lines.stream().map(ReservationLine::productId).collect(Collectors.toUnmodifiableSet());
    }

    private void transitionTo(final ReservationStatus target, final Instant now) {

        if (now == null) {
            throw new DomainValidationException(StockReservationErrorCode.TIMESTAMP_REQUIRED);
        }

        if (!status.canTransitionTo(target)) {
            throw new InvalidReservationStateException(id, status, target);
        }

        this.status = target;
        this.lastModifiedAt = now;
    }

    private static Optional<StockRejection> firstRejection(final List<RequestedLine> lines, final Map<ProductId, Product> products) {

        final Optional<StockRejection> unknown = first(lines, line -> !products.containsKey(line.productId()), RejectionReason.UNKNOWN_PRODUCT);
        if (unknown.isPresent()) {
            return unknown;
        }

        final Optional<StockRejection> priceChanged = first(lines, line -> !products.get(line.productId()).hasUnitPrice(line.unitPrice()), RejectionReason.PRICE_CHANGED);
        if (priceChanged.isPresent()) {
            return priceChanged;
        }

        return first(lines, line -> !products.get(line.productId()).canReserve(line.quantity()), RejectionReason.INSUFFICIENT_STOCK);
    }

    private static Optional<StockRejection> first(final List<RequestedLine> lines, final Predicate<RequestedLine> fails, final RejectionReason reason) {
        return lines.stream()
                .filter(fails)
                .findFirst()
                .map(line -> new StockRejection(reason, line.productId()));
    }

    private static Optional<ProductId> findDuplicateProduct(final List<ReservationLine> lines) {
        final Set<ProductId> seen = new HashSet<>();
        return lines.stream()
                .map(ReservationLine::productId)
                .filter(productId -> !seen.add(productId))
                .findFirst();
    }
}
