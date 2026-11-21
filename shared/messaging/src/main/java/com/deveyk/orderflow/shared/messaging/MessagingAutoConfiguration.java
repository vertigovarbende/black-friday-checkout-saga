package com.deveyk.orderflow.shared.messaging;

import com.deveyk.orderflow.shared.observability.ObservabilityAutoConfiguration;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@AutoConfiguration(after = ObservabilityAutoConfiguration.class)
public class MessagingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventBuilder eventBuilder(final ObjectProvider<TraceContextProvider> traceContextProvider,
                                     final ObjectProvider<Clock> clock) {

        return new EventBuilder(traceContextProvider.getIfAvailable(() -> new TraceContextProvider(Tracer.NOOP)), clock.getIfAvailable(Clock::systemUTC));

    }

}
