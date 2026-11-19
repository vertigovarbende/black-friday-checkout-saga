package com.deveyk.orderflow.order.domain.product.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.shared.domain.AggregateRoot;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

// Ürünün order-service'teki yerel kopyası. Şimdilik sadece okunur; product.changed ile güncellenmesi Faz 2 adım 7'de.
@Getter
public class ProductSnapshot extends AggregateRoot<ProductId> {

    public static final int MAX_NAME_LENGTH = 200;

    private final ProductId id;
    private final String name;
    private final Money unitPrice;

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private ProductSnapshot(final ProductId id, final String name, final Money unitPrice, final long version) {

        if (id == null) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_ID_REQUIRED);
        }

        if (name == null || name.isBlank()) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_NAME_REQUIRED);
        }

        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_NAME_TOO_LONG,
                    String.valueOf(MAX_NAME_LENGTH));
        }

        if (unitPrice == null) {
            throw new DomainValidationException(ProductSnapshotErrorCode.UNIT_PRICE_REQUIRED);
        }

        if (!unitPrice.isPositive()) {
            throw new DomainValidationException(ProductSnapshotErrorCode.UNIT_PRICE_MUST_BE_POSITIVE);
        }

        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        restoreVersion(version);
    }
}
