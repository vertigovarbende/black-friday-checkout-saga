package com.deveyk.orderflow.product.domain.reservation.model;

// stock.reserve.failed'in sebepleri (event-catalog.md §3). RESERVATION_CLOSED saklanmaz: kapanmış bir rezervasyona
// gelen reserve isteğinin cevabıdır (checkout-saga.md §6).
public enum RejectionReason {

    UNKNOWN_PRODUCT,
    PRICE_CHANGED,
    INSUFFICIENT_STOCK,
    RESERVATION_CLOSED
}
