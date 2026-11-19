package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.order.exception.InvalidOrderStateException;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.shared.domain.AggregateRoot;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
public class Order extends AggregateRoot<OrderId> {

    public static final int MAX_LINES = 50;

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final Instant placedAt;
    private OrderStatus status;
    @Getter(AccessLevel.NONE)
    private CancellationReason cancellationReason;
    private Instant lastModifiedAt;

    public static Order place(final CustomerId customerId, final List<OrderLine> lines, final Instant now) {

        if (lines != null && lines.size() > MAX_LINES) {
            throw new DomainValidationException(OrderErrorCode.TOO_MANY_LINES, String.valueOf(MAX_LINES));
        }

        return new Order(OrderId.newId(), customerId, lines, now, OrderStatus.PENDING, null, now, 0L);
    }

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private Order(final OrderId id, final CustomerId customerId, final List<OrderLine> lines, final Instant placedAt,
                  final OrderStatus status, final CancellationReason cancellationReason,
                  final Instant lastModifiedAt, final long version) {

        if (id == null) {
            throw new DomainValidationException(OrderErrorCode.ORDER_ID_REQUIRED);
        }

        if (customerId == null) {
            throw new DomainValidationException(OrderErrorCode.CUSTOMER_REQUIRED);
        }

        if (lines == null || lines.isEmpty() || lines.stream().anyMatch(Objects::isNull)) {
            throw new DomainValidationException(OrderErrorCode.ORDER_LINES_REQUIRED);
        }

        if (hasMixedCurrencies(lines)) {
            throw new DomainValidationException(OrderErrorCode.MIXED_CURRENCY);
        }

        final Optional<ProductId> duplicateProduct = findDuplicateProduct(lines);
        if (duplicateProduct.isPresent()) {
            throw new DomainValidationException(OrderErrorCode.DUPLICATE_PRODUCT_LINE, duplicateProduct.get().value().toString());
        }

        if (placedAt == null) {
            throw new DomainValidationException(OrderErrorCode.PLACED_AT_REQUIRED);
        }

        if (status == null) {
            throw new DomainValidationException(OrderErrorCode.ORDER_STATUS_REQUIRED);
        }

        if (status == OrderStatus.CANCELLED && cancellationReason == null) {
            throw new DomainValidationException(OrderErrorCode.CANCELLATION_REASON_REQUIRED);
        }

        if (status != OrderStatus.CANCELLED && cancellationReason != null) {
            throw new DomainValidationException(OrderErrorCode.CANCELLATION_REASON_NOT_ALLOWED, status.name());
        }

        if (lastModifiedAt == null) {
            throw new DomainValidationException(OrderErrorCode.LAST_MODIFIED_AT_REQUIRED);
        }

        this.id = id;
        this.customerId = customerId;
        this.lines = List.copyOf(lines);
        this.placedAt = placedAt;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.lastModifiedAt = lastModifiedAt;
        restoreVersion(version);
    }

    public void complete(final Instant now) {

        if (status == OrderStatus.COMPLETED) {
            return;
        }

        transitionTo(OrderStatus.COMPLETED, now);
    }

    public void cancel(final CancellationReason reason, final Instant now) {
        if (reason == null) {
            throw new DomainValidationException(OrderErrorCode.CANCELLATION_REASON_REQUIRED);
        }

        if (status == OrderStatus.CANCELLED) {
            return;
        }

        transitionTo(OrderStatus.CANCELLED, now);
        this.cancellationReason = reason;
    }

    public Money getTotal() {
        return lines.stream().map(OrderLine::getLineTotal).reduce(Money::add).orElseThrow();
    }

    public Optional<CancellationReason> getCancellationReason() {
        return Optional.ofNullable(cancellationReason);
    }

    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }

    private void transitionTo(final OrderStatus target, final Instant now) {

        if (now == null) {
            throw new DomainValidationException(OrderErrorCode.TIMESTAMP_REQUIRED);
        }

        if (!status.canTransitionTo(target)) {
            throw new InvalidOrderStateException(id, status, target);
        }

        this.status = target;
        this.lastModifiedAt = now;
    }

    private static boolean hasMixedCurrencies(final List<OrderLine> lines) {
        final Currency first = lines.getFirst().getUnitPrice().currency();
        return lines.stream().anyMatch(line -> !line.getUnitPrice().currency().equals(first));
    }

    private static Optional<ProductId> findDuplicateProduct(final List<OrderLine> lines) {
        final Set<ProductId> seen = new HashSet<>();
        return lines.stream().map(OrderLine::getProductId).filter(productId -> !seen.add(productId)).findFirst();
    }
}
