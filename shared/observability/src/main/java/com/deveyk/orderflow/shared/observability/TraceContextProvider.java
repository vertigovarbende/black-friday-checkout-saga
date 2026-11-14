package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import java.util.Optional;

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
