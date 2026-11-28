package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.shared.persistence.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "stock_reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StockReservationEntity extends BaseEntity {

    // orderId
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 30)
    private RejectionReason rejectionReason;

    @Column(name = "rejected_product_id")
    private UUID rejectedProductId;

    // Domain'in createdAt'i (iş zamanı); BaseEntity'nin created_at'i teknik zamandır.
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    @ElementCollection
    @CollectionTable(name = "stock_reservation_lines", joinColumns = @JoinColumn(name = "reservation_id"))
    private List<StockReservationLineEmbeddable> lines = new ArrayList<>();

    @Builder
    private StockReservationEntity(final UUID id,
                                   final ReservationStatus status,
                                   final RejectionReason rejectionReason,
                                   final UUID rejectedProductId,
                                   final Instant requestedAt,
                                   final Instant lastModifiedAt) {
        this.id = id;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.rejectedProductId = rejectedProductId;
        this.requestedAt = requestedAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    void addLine(final StockReservationLineEmbeddable line) {
        lines.add(line);
    }

    // Satırlar ve ret bilgisi oluşturulduktan sonra değişmez; sadece durum alanları güncellenir.
    void applyState(final ReservationStatus status, final Instant lastModifiedAt) {
        this.status = status;
        this.lastModifiedAt = lastModifiedAt;
    }
}
