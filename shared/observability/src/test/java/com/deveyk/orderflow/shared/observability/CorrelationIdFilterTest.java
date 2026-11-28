package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    private final AtomicReference<String> correlationIdInChain = new AtomicReference<>();

    private final FilterChain recordingChain = (request, response) -> correlationIdInChain.set(MDC.get(MdcKeys.CORRELATION_ID));

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("Geçerli X-Correlation-Id kabul edilir: istek boyunca MDC'de, response'ta aynen; X-Trace-Id de yazılır")
    void givenValidCorrelationHeader_whenFilter_thenUsedInMdcAndEchoed() throws Exception {
        // Given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(OrderflowHeaders.CORRELATION_ID, "checkout-2026-11-27-42");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        filterWithTrace().doFilter(request, response, recordingChain);

        // Then
        assertThat(correlationIdInChain.get()).isEqualTo("checkout-2026-11-27-42");
        assertThat(response.getHeader(OrderflowHeaders.CORRELATION_ID)).isEqualTo("checkout-2026-11-27-42");
        assertThat(response.getHeader(OrderflowHeaders.TRACE_ID)).isEqualTo(TRACE_ID);
        assertThat(MDC.get(MdcKeys.CORRELATION_ID)).isNull();
    }

    @Test
    @DisplayName("Header yoksa correlationId akışın başındaki traceId'dir")
    void givenNoCorrelationHeader_whenFilter_thenTraceIdUsed() throws Exception {
        // Given
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        filterWithTrace().doFilter(new MockHttpServletRequest(), response, recordingChain);

        // Then
        assertThat(correlationIdInChain.get()).isEqualTo(TRACE_ID);
        assertThat(response.getHeader(OrderflowHeaders.CORRELATION_ID)).isEqualTo(TRACE_ID);
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "has space", "<script>", "checkout_42"})
    @DisplayName("Geçersiz X-Correlation-Id kabul edilmez (sadece [A-Za-z0-9-]); yerine traceId kullanılır")
    void givenInvalidCorrelationHeader_whenFilter_thenTraceIdUsed(final String header) throws Exception {
        // Given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(OrderflowHeaders.CORRELATION_ID, header);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        filterWithTrace().doFilter(request, response, recordingChain);

        // Then
        assertThat(correlationIdInChain.get()).isEqualTo(TRACE_ID);
        assertThat(response.getHeader(OrderflowHeaders.CORRELATION_ID)).isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("64 karakterden uzun X-Correlation-Id kabul edilmez")
    void givenTooLongCorrelationHeader_whenFilter_thenTraceIdUsed() throws Exception {
        // Given
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(OrderflowHeaders.CORRELATION_ID, "a".repeat(65));

        // When
        filterWithTrace().doFilter(request, new MockHttpServletResponse(), recordingChain);

        // Then
        assertThat(correlationIdInChain.get()).isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("Tracing kapalı ve header yoksa korelasyon kimliği uydurulmaz; istek yine işlenir")
    void givenNoTraceAndNoHeader_whenFilter_thenNoHeadersAndChainCalled() throws Exception {
        // Given
        final AtomicBoolean chainCalled = new AtomicBoolean();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        new CorrelationIdFilter(new TraceContextProvider(Tracer.NOOP))
                .doFilter(new MockHttpServletRequest(), response, (req, res) -> chainCalled.set(true));

        // Then
        assertThat(chainCalled).isTrue();
        assertThat(response.getHeader(OrderflowHeaders.CORRELATION_ID)).isNull();
        assertThat(response.getHeader(OrderflowHeaders.TRACE_ID)).isNull();
    }

    @Test
    @DisplayName("Filter MDC'den sadece kendi anahtarını siler; tracing'in koyduğu anahtarlar kalır (MDC.clear() yok)")
    void givenOtherMdcKeys_whenFilter_thenOnlyCorrelationIdRemoved() throws Exception {
        // Given
        MDC.put("traceId", TRACE_ID);

        // When
        filterWithTrace().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), recordingChain);

        // Then
        assertThat(MDC.get("traceId")).isEqualTo(TRACE_ID);
        assertThat(MDC.get(MdcKeys.CORRELATION_ID)).isNull();
    }

    private static CorrelationIdFilter filterWithTrace() {
        final Tracer tracer = Mockito.mock(Tracer.class);
        final Span span = Mockito.mock(Span.class);
        final TraceContext context = Mockito.mock(TraceContext.class);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(context);
        Mockito.when(context.traceId()).thenReturn(TRACE_ID);
        return new CorrelationIdFilter(new TraceContextProvider(tracer));
    }
}
