package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
}
