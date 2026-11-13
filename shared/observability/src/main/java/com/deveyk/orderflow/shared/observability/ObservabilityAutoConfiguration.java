package com.deveyk.orderflow.shared.observability;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * shared-observability'nin bean'leri. Servislerin component scan'i {@code com.deveyk.orderflow.<servis>}
 * ile sınırlı olduğu için shared bean'ler auto-configuration ile gelir.
 */
@AutoConfiguration
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceContextProvider traceContextProvider(final ObjectProvider<Tracer> tracer) {
        return new TraceContextProvider(tracer.getIfAvailable(() -> Tracer.NOOP));
    }
}
