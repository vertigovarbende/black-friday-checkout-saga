package com.deveyk.orderflow.order.domain.product.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.shared.domain.AggregateRoot;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

// Ürünün order-service'teki yerel kopyası; product.changed ile oluşturulur ve güncellenir
// productVersion ürünün product-service'teki sürümüdür; AggregateRoot.version ise bu kopyanın optimistic lock sürümü.
@Getter
public class ProductSnapshot extends AggregateRoot<ProductId> {

    public static final int MAX_NAME_LENGTH = 200;

    private final ProductId id;
    private String name;
    private Money unitPrice;
    private long productVersion;

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private ProductSnapshot(final ProductId id,
                            final String name,
                            final Money unitPrice,
                            final long productVersion,
                            final long version) {

        if (id == null) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_ID_REQUIRED);
        }

        validate(name, unitPrice, productVersion);

        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.productVersion = productVersion;
        restoreVersion(version);
    }

    public static ProductSnapshot create(final ProductId id,
                                         final String name,
                                         final Money unitPrice,
                                         final long productVersion) {

        return new ProductSnapshot(id, name, unitPrice, productVersion, 0L);
    }

    // Daha yeni bir sürümü uygular. Aynı ya da eski sürüm (yeniden yayın, geç gelen kopya) hiçbir şeyi değiştirmez: false.
    public boolean applyChange(final String name, final Money unitPrice, final long productVersion) {

        if (productVersion <= this.productVersion) {
            return false;
        }

        validate(name, unitPrice, productVersion);

        this.name = name;
        this.unitPrice = unitPrice;
        this.productVersion = productVersion;
        return true;
    }

    private static void validate(final String name, final Money unitPrice, final long productVersion) {

        if (name == null || name.isBlank()) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_NAME_REQUIRED);
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_NAME_TOO_LONG, String.valueOf(MAX_NAME_LENGTH));
        }

        if (unitPrice == null) {
            throw new DomainValidationException(ProductSnapshotErrorCode.UNIT_PRICE_REQUIRED);
        }

        if (!unitPrice.isPositive()) {
            throw new DomainValidationException(ProductSnapshotErrorCode.UNIT_PRICE_MUST_BE_POSITIVE);
        }

        if (productVersion < 0) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_VERSION_NEGATIVE, String.valueOf(productVersion));
        }

    }
}
