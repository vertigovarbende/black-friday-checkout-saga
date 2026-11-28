package com.deveyk.orderflow.order.domain.order.exception;

import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;

import java.io.Serial;

public final class InvalidOrderStateException extends OrderflowInvalidStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidOrderStateException(final OrderId orderId, final OrderStatus current, final OrderStatus target) {
        super(OrderErrorCode.INVALID_ORDER_STATE, orderId.value().toString(), current.name(), target.name());
    }
}
