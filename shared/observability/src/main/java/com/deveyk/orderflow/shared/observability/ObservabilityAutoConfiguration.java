package com.deveyk.orderflow.shared.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceContextProvider traceContextProvider(final ObjectProvider<Tracer> tracer) {
        return new TraceContextProvider(tracer.getIfAvailable(() -> Tracer.NOOP));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class CorrelationIdFilterConfiguration {

        @Bean
        @ConditionalOnMissingBean
        CorrelationIdFilter correlationIdFilter(final TraceContextProvider traceContextProvider) {
            return new CorrelationIdFilter(traceContextProvider);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    static class UseCaseObservationConfiguration {

        @Bean
        @ConditionalOnMissingBean
        UseCaseObservationAspect useCaseObservationAspect(final ObjectProvider<ObservationRegistry> observationRegistry) {
            return new UseCaseObservationAspect(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP));
        }
    }
}
