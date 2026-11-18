package com.deveyk.orderflow.order.adapter.out.persistence.order;

import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.shared.persistence.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 30)
    private CancellationReason cancellationReason;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo")
    private List<OrderLineEntity> lines = new ArrayList<>();

    @Builder
    private OrderEntity(final UUID id, final UUID customerId, final OrderStatus status,
                        final CancellationReason cancellationReason, final Instant placedAt,
                        final Instant lastModifiedAt) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.placedAt = placedAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    void addLine(final OrderLineEntity line) {
        lines.add(line);
    }

    // Satırlar sipariş verildikten sonra değişmez; sadece durum alanları güncellenir.
    void applyState(final OrderStatus status, final CancellationReason cancellationReason, final Instant lastModifiedAt) {
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.lastModifiedAt = lastModifiedAt;
    }
}
