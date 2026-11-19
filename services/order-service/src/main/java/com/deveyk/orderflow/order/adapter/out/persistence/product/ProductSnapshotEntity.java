package com.deveyk.orderflow.order.adapter.out.persistence.product;

import com.deveyk.orderflow.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

// Şimdilik sadece okunur (satırlar seed'den gelir). Yazma yolu product.changed ile adım 7'de eklenir.
@Getter
@Entity
@Table(name = "product_snapshots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ProductSnapshotEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}
