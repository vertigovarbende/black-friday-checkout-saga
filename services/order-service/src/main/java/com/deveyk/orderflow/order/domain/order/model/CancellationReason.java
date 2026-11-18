package com.deveyk.orderflow.order.domain.order.model;

public enum CancellationReason {

    INSUFFICIENT_STOCK,
    PAYMENT_DECLINED,
    SAGA_TIMEOUT
}
