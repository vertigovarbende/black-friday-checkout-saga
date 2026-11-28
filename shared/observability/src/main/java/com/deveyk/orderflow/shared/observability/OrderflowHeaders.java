package com.deveyk.orderflow.shared.observability;

// Servislerin ortak HTTP header adları
public final class OrderflowHeaders {

    public static final String USER_ID = "X-User-Id";                  // Auth kapsam dışı: isteği yapan kullanıcının kimliği (order-service'te CustomerId).
    public static final String CORRELATION_ID = "X-Correlation-Id";    // İş akışının kimliği: istemci verebilir; verilmezse akışın başındaki traceId.
    public static final String TRACE_ID = "X-Trace-Id";                // Sadece response'ta: isteğin trace'i (Tempo'da aramak için).

    private OrderflowHeaders() {
    }
}
