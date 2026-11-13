package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import java.util.Optional;

/**
 * O anki trace'in kimliğini verir (08-loglama-kararlari §5).
 *
 * <p>Tracing kapalıysa ya da o an bir span yoksa boş döner. {@link Tracer#NOOP} boş bir trace id
 * ({@code ""}) verdiği için boş değerler de "yok" sayılır.
 *
 * <p>Kullananlar: {@code ErrorResponse.traceId} (shared-web); ileride {@code CorrelationIdFilter},
 * {@code EventBuilder} ve outbox'ın {@code traceparent} kolonu.
 */
public class TraceContextProvider {

    private final Tracer tracer;

    public TraceContextProvider(final Tracer tracer) {
        this.tracer = tracer;
    }

    public Optional<String> currentTraceId() {
        final Span span = tracer.currentSpan();
        if (span == null) {
            return Optional.empty();
        }
        final String traceId = span.context().traceId();
        return traceId == null || traceId.isBlank() ? Optional.empty() : Optional.of(traceId);
    }
}
