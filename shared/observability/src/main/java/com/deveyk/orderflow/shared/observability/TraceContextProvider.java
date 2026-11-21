package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TraceContextProvider {

    private final Tracer tracer;
    private final Propagator propagator;

    public TraceContextProvider(final Tracer tracer) {
        this(tracer, Propagator.NOOP);
    }

    public TraceContextProvider(final Tracer tracer, final Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    public Optional<String> currentTraceId() {
        final Span span = tracer.currentSpan();

        if (span == null) {
            return Optional.empty();
        }

        final String traceId = span.context().traceId();

        if (traceId == null || traceId.isBlank())
            return Optional.empty();

        return Optional.of(traceId);

    }

    public Optional<String> currentTraceparent() {
        final Span span = tracer.currentSpan();

        if (span == null) {
            return Optional.empty();
        }

        final Map<String, String> carrier = new HashMap<>();
        propagator.inject(span.context(), carrier, Map::put);

        return Optional.ofNullable(carrier.get("traceparent"));
    }
}
