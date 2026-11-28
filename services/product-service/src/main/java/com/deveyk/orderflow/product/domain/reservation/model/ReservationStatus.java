package com.deveyk.orderflow.product.domain.reservation.model;

// RESERVED → COMMITTED | RELEASED; REJECTED baştan son durumdur (05 §8).
public enum ReservationStatus {

    RESERVED,
    COMMITTED,
    RELEASED,
    REJECTED;

    public boolean canTransitionTo(final ReservationStatus target) {
        return switch (this) {
            case RESERVED -> target == COMMITTED || target == RELEASED;
            case COMMITTED, RELEASED, REJECTED -> false;
        };
    }
}
