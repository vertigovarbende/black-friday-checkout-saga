package com.deveyk.orderflow.shared.observability;

// Servislerin ortak HTTP header adları
public final class OrderflowHeaders {

    // Auth kapsam dışı: isteği yapan kullanıcının kimliği (order-service'te CustomerId).
    public static final String USER_ID = "X-User-Id";

    private OrderflowHeaders() {
    }
}
