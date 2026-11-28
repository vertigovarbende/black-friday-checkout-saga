package com.deveyk.orderflow.product.adapter.out.persistence.product;

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

// Satırlar seed'den gelir; bu adımda ürün oluşturulmaz, sadece güncellenir.
@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ProductEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "on_hand", nullable = false)
    private int onHand;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "catalog_version", nullable = false)
    private long catalogVersion;

    void applyState(final String name, final BigDecimal unitPrice, final String currency,
                    final int onHand, final int reserved, final long catalogVersion) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.onHand = onHand;
        this.reserved = reserved;
        this.catalogVersion = catalogVersion;
    }
}
