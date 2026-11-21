package com.deveyk.orderflow.shared.outbox;

import com.deveyk.orderflow.shared.observability.ObservabilityAutoConfiguration;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;

@AutoConfiguration(
        before = {HibernateJpaAutoConfiguration.class, DataJpaRepositoriesAutoConfiguration.class},
        after = ObservabilityAutoConfiguration.class)
@AutoConfigurationPackage(basePackageClasses = OutboxAutoConfiguration.class)
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    OutboxEventPort outboxEventPort(final OutboxEventRepository outboxEventRepository,
                                    final JsonMapper jsonMapper,
                                    final ObjectProvider<TraceContextProvider> traceContextProvider,
                                    final ObjectProvider<Clock> clock) {

        return new OutboxEventAdapter(outboxEventRepository, jsonMapper, traceContextProvider.getIfAvailable(() -> new TraceContextProvider(Tracer.NOOP)), clock.getIfAvailable(Clock::systemUTC));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(prefix = "orderflow.outbox.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class OutboxCleanupConfiguration {

        @Bean
        OutboxCleanupJob outboxCleanupJob(final OutboxEventRepository outboxEventRepository,
                                          final ObjectProvider<Clock> clock,
                                          final OutboxProperties properties) {

            return new OutboxCleanupJob(outboxEventRepository, clock.getIfAvailable(Clock::systemUTC), properties.cleanup().retention());
        }
    }
}
