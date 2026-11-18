package com.deveyk.orderflow.order.domain.order.model;

import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.shared.domain.Entity;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderLine extends Entity<OrderLineId> {

    public static final int MAX_PRODUCT_NAME_LENGTH = 200;

    private final OrderLineId id;
    private final ProductId productId;
    private final String productName;
    private final Money unitPrice;
    private final Quantity quantity;

    public static OrderLine create(final ProductId productId, final String productName,
                                   final Money unitPrice, final Quantity quantity) {
        return new OrderLine(OrderLineId.newId(), productId, productName, unitPrice, quantity);
    }

    @Builder(builderMethodName = "rehydrate", builderClassName = "Rehydrator")
    private OrderLine(final OrderLineId id, final ProductId productId, final String productName,
                      final Money unitPrice, final Quantity quantity) {

        if (id == null) {
            throw new DomainValidationException(OrderErrorCode.ORDER_LINE_ID_REQUIRED);
        }

        if (productId == null) {
            throw new DomainValidationException(OrderErrorCode.PRODUCT_REQUIRED);
        }

        if (productName == null || productName.isBlank()) {
            throw new DomainValidationException(OrderErrorCode.PRODUCT_NAME_REQUIRED);
        }

        if (productName.length() > MAX_PRODUCT_NAME_LENGTH) {
            throw new DomainValidationException(OrderErrorCode.PRODUCT_NAME_TOO_LONG,
                    String.valueOf(MAX_PRODUCT_NAME_LENGTH));
        }

        if (unitPrice == null) {
            throw new DomainValidationException(OrderErrorCode.UNIT_PRICE_REQUIRED);
        }

        if (!unitPrice.isPositive()) {
            throw new DomainValidationException(OrderErrorCode.UNIT_PRICE_MUST_BE_POSITIVE);
        }

        if (quantity == null) {
            throw new DomainValidationException(OrderErrorCode.QUANTITY_REQUIRED);
        }

        if (!quantity.isPositive()) {
            throw new DomainValidationException(OrderErrorCode.QUANTITY_MUST_BE_POSITIVE);
        }

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }
}
