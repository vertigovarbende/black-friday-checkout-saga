package com.deveyk.orderflow.product.domain.product.model;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.exception.InsufficientStockException;
import com.deveyk.orderflow.product.domain.product.exception.ProductErrorCode;
import com.deveyk.orderflow.shared.domain.AggregateRoot;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

// Ürün ve stoğu (05 §8): kullanılabilir = onHand − reserved, değişmez 0 ≤ reserved ≤ onHand.
// catalogVersion product.changed'in productVersion'ıdır: sadece ad ya da fiyat değişince artar. AggregateRoot.version
// (optimistic lock) her rezervasyonda da artar ve flush'ta değiştiği için event'e konamaz.
@Getter
public class Product extends AggregateRoot<ProductId> {

    public static final int MAX_NAME_LENGTH = 200;

    private final ProductId id;
    private String name;
    private Money unitPrice;
    private Quantity onHand;
    private Quantity reserved;
    private long catalogVersion;

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private Product(final ProductId id,
                    final String name,
                    final Money unitPrice,
                    final Quantity onHand,
                    final Quantity reserved,
                    final long catalogVersion,
                    final long version) {

        if (id == null) {
            throw new DomainValidationException(ProductErrorCode.PRODUCT_ID_REQUIRED);
        }

        validateDetails(name, unitPrice);

        if (onHand == null || reserved == null) {
            throw new DomainValidationException(ProductErrorCode.STOCK_REQUIRED);
        }

        if (!onHand.isAtLeast(reserved)) {
            throw new DomainValidationException(ProductErrorCode.RESERVED_EXCEEDS_ON_HAND, id.value().toString(),
                    String.valueOf(reserved.value()), String.valueOf(onHand.value()));
        }

        if (catalogVersion < 1) {
            throw new DomainValidationException(ProductErrorCode.CATALOG_VERSION_INVALID, String.valueOf(catalogVersion));
        }

        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.onHand = onHand;
        this.reserved = reserved;
        this.catalogVersion = catalogVersion;
        restoreVersion(version);
    }

    public Quantity available() {
        return onHand.minus(reserved);
    }

    public boolean canReserve(final Quantity quantity) {
        return available().isAtLeast(quantity);
    }

    public boolean hasUnitPrice(final Money price) {
        return unitPrice.equals(price);
    }

    // Saga T2.
    public void reserve(final Quantity quantity) {

        if (!canReserve(quantity)) {
            throw new InsufficientStockException(id, available(), quantity);
        }

        reserved = reserved.plus(quantity);
    }

    // Telafi C2: ayrılan stok kullanılabilire döner.
    public void release(final Quantity quantity) {
        requireReserved(quantity);
        reserved = reserved.minus(quantity);
    }

    // order.completed: satış kesinleşir, ayrılan stok depodan düşer.
    public void commit(final Quantity quantity) {
        requireReserved(quantity);
        reserved = reserved.minus(quantity);
        onHand = onHand.minus(quantity);
    }

    // Ad ya da fiyat gerçekten değiştiyse catalogVersion artar ve true döner (product.changed yayınlanır).
    public boolean changeDetails(final String name, final Money unitPrice) {

        validateDetails(name, unitPrice);

        if (this.name.equals(name) && hasUnitPrice(unitPrice)) {
            return false;
        }

        this.name = name;
        this.unitPrice = unitPrice;
        this.catalogVersion++;
        return true;
    }

    private void requireReserved(final Quantity quantity) {
        if (!reserved.isAtLeast(quantity)) {
            throw new DomainValidationException(ProductErrorCode.RESERVED_STOCK_INSUFFICIENT, id.value().toString(),
                    String.valueOf(reserved.value()), String.valueOf(quantity.value()));
        }
    }

    private static void validateDetails(final String name, final Money unitPrice) {

        if (name == null || name.isBlank()) {
            throw new DomainValidationException(ProductErrorCode.PRODUCT_NAME_REQUIRED);
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(ProductErrorCode.PRODUCT_NAME_TOO_LONG, String.valueOf(MAX_NAME_LENGTH));
        }

        if (unitPrice == null) {
            throw new DomainValidationException(ProductErrorCode.UNIT_PRICE_REQUIRED);
        }

        if (!unitPrice.isPositive()) {
            throw new DomainValidationException(ProductErrorCode.UNIT_PRICE_MUST_BE_POSITIVE);
        }
    }
}
