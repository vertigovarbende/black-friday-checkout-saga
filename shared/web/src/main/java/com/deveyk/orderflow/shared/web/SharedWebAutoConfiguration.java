package com.deveyk.orderflow.shared.web;

import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import com.deveyk.orderflow.shared.web.error.GlobalExceptionHandler;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SharedWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(final ObjectProvider<TraceContextProvider> traceContextProvider,
                                                         final ObjectProvider<MeterRegistry> meterRegistry) {
        return new GlobalExceptionHandler(traceContextProvider, meterRegistry);
    }
}
