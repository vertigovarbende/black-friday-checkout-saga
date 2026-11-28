package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// Rezervasyonun satırı. Satırlar oluşturulduktan sonra değişmez; bu yüzden ayrı bir entity değil, @ElementCollection.
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class StockReservationLineEmbeddable {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    StockReservationLineEmbeddable(final UUID productId, final int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
