package com.deveyk.orderflow.order.domain.order.model;

public enum OrderStatus {

    PENDING,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(final OrderStatus target) {
        return switch (this) {
            case PENDING -> target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
