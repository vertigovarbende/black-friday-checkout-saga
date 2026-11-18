package com.deveyk.orderflow.order.domain.order.exception;

import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;

import java.io.Serial;

public final class OrderNotFoundException extends OrderflowResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(final OrderId orderId) {
        super(OrderErrorCode.ORDER_NOT_FOUND, orderId.value().toString());
    }
}
