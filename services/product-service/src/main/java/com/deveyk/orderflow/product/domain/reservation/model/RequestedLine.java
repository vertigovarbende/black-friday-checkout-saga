package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

// stock.reserve.requested'in bir satırı. unitPrice siparişin ürün kopyasındaki fiyattır; fiyatın kesin kontrolü
// burada yapılır (02 §3.1 kural 3).
public record RequestedLine(ProductId productId, Quantity quantity, Money unitPrice) {

    public RequestedLine {

        if (productId == null) {
            throw new DomainValidationException(StockReservationErrorCode.LINE_PRODUCT_REQUIRED);
        }

        if (quantity == null || !quantity.isPositive()) {
            throw new DomainValidationException(StockReservationErrorCode.LINE_QUANTITY_MUST_BE_POSITIVE);
        }

        if (unitPrice == null) {
            throw new DomainValidationException(StockReservationErrorCode.LINE_UNIT_PRICE_REQUIRED);
        }
    }

    public ReservationLine toReservationLine() {
        return new ReservationLine(productId, quantity);
    }
}
