package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TraceContextProviderTest {

    @Test
    @DisplayName("Aktif bir span varsa onun trace id'si döner")
    void givenCurrentSpan_whenCurrentTraceId_thenReturnTraceId() {
        // Given
        final Tracer tracer = Mockito.mock(Tracer.class);
        final Span span = Mockito.mock(Span.class);
        final TraceContext context = Mockito.mock(TraceContext.class);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(context);
        Mockito.when(context.traceId()).thenReturn("4bf92f3577b34da6a3ce929d0e0e4736");

        // When
        final Optional<String> traceId = new TraceContextProvider(tracer).currentTraceId();

        // Then
        assertThat(traceId).contains("4bf92f3577b34da6a3ce929d0e0e4736");
    }

    @Test
    @DisplayName("Aktif span yoksa boş döner")
    void givenNoCurrentSpan_whenCurrentTraceId_thenEmpty() {
        // Given
        final Tracer tracer = Mockito.mock(Tracer.class);
        Mockito.when(tracer.currentSpan()).thenReturn(null);

        // When
        final Optional<String> traceId = new TraceContextProvider(tracer).currentTraceId();

        // Then
        assertThat(traceId).isEmpty();
    }

    @Test
    @DisplayName("Tracing kapalıyken (NOOP tracer) boş döner")
    void givenNoopTracer_whenCurrentTraceId_thenEmpty() {
        // When
        final Optional<String> traceId = new TraceContextProvider(Tracer.NOOP).currentTraceId();

        // Then
        assertThat(traceId).isEmpty();
    }

    @Test
    @DisplayName("Aktif span varsa propagator'ın ürettiği W3C traceparent döner")
    void givenCurrentSpan_whenCurrentTraceparent_thenReturnInjectedTraceparent() {
        // Given
        final Tracer tracer = Mockito.mock(Tracer.class);
        final Span span = Mockito.mock(Span.class);
        final TraceContext context = Mockito.mock(TraceContext.class);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(context);
        final String traceparent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";
        final Propagator propagator = new Propagator() {
            @Override
            public List<String> fields() {
                return List.of("traceparent");
            }

            @Override
            public <C> void inject(final TraceContext traceContext, final C carrier, final Setter<C> setter) {
                setter.set(carrier, "traceparent", traceparent);
            }

            @Override
            public <C> Span.Builder extract(final C carrier, final Getter<C> getter) {
                throw new UnsupportedOperationException();
            }
        };

        // When
        final Optional<String> result = new TraceContextProvider(tracer, propagator).currentTraceparent();

        // Then
        assertThat(result).contains(traceparent);
    }

    @Test
    @DisplayName("Aktif span yoksa ya da propagator yoksa traceparent boş döner")
    void givenNoSpanOrNoopPropagator_whenCurrentTraceparent_thenEmpty() {
        // Given
        final Tracer noSpan = Mockito.mock(Tracer.class);
        Mockito.when(noSpan.currentSpan()).thenReturn(null);
        final Tracer withSpan = Mockito.mock(Tracer.class);
        final Span span = Mockito.mock(Span.class);
        Mockito.when(withSpan.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(Mockito.mock(TraceContext.class));

        // When / Then
        assertThat(new TraceContextProvider(noSpan, Propagator.NOOP).currentTraceparent()).isEmpty();
        assertThat(new TraceContextProvider(withSpan, Propagator.NOOP).currentTraceparent()).isEmpty();
    }
}
