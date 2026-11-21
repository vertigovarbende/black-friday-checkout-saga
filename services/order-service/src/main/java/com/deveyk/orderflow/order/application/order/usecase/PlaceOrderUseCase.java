package com.deveyk.orderflow.order.application.order.usecase;

import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

import java.util.List;
import java.util.Objects;

public record PlaceOrderUseCase(CustomerId customerId, List<Line> lines) implements UseCase {

    public PlaceOrderUseCase {

        if (customerId == null) {
            throw new DomainValidationException(OrderErrorCode.CUSTOMER_REQUIRED);
        }

        if (lines == null || lines.isEmpty() || lines.stream().anyMatch(Objects::isNull)) {
            throw new DomainValidationException(OrderErrorCode.ORDER_LINES_REQUIRED);
        }

        lines = List.copyOf(lines);
    }

    // Ad ve fiyat istekte yoktur; handler ürün kopyasından alır.
    public record Line(ProductId productId, Quantity quantity) {

        public Line {

            if (productId == null) {
                throw new DomainValidationException(OrderErrorCode.PRODUCT_REQUIRED);
            }

            if (quantity == null) {
                throw new DomainValidationException(OrderErrorCode.QUANTITY_REQUIRED);
            }

        }
    }
}
