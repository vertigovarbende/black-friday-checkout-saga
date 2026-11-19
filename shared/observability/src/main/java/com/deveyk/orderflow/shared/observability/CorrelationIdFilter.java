package com.deveyk.orderflow.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class CorrelationIdFilter extends OncePerRequestFilter implements Ordered {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

    private final TraceContextProvider traceContextProvider;

    public CorrelationIdFilter(final TraceContextProvider traceContextProvider) {
        this.traceContextProvider = traceContextProvider;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final String traceId = traceContextProvider.currentTraceId().orElse(null);
        final String correlationId = Optional.ofNullable(request.getHeader(OrderflowHeaders.CORRELATION_ID))
                .filter(CorrelationIdFilter::isValid)
                .orElse(traceId);

        if (traceId != null) {
            response.setHeader(OrderflowHeaders.TRACE_ID, traceId);
        }

        // Tracing kapalı ve header yoksa korelasyon kimliği yok; uydurulmaz.
        if (correlationId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader(OrderflowHeaders.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    static boolean isValid(final String correlationId) {
        return VALID_CORRELATION_ID.matcher(correlationId).matches();
    }
}
