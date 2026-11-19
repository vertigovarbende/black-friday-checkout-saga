package com.deveyk.orderflow.order.application.order.usecase;

import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.shared.application.UseCase;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

public record GetOrderUseCase(OrderId orderId, CustomerId customerId) implements UseCase {

    public GetOrderUseCase {

        if (orderId == null) {
            throw new DomainValidationException(OrderErrorCode.ORDER_ID_REQUIRED);
        }

        if (customerId == null) {
            throw new DomainValidationException(OrderErrorCode.CUSTOMER_REQUIRED);
        }
    }
}
