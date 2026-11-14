package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceContextProvider traceContextProvider(final ObjectProvider<Tracer> tracer) {
        return new TraceContextProvider(tracer.getIfAvailable(() -> Tracer.NOOP));
    }
}
