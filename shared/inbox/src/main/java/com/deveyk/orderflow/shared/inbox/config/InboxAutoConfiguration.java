package com.deveyk.orderflow.shared.inbox.config;

import com.deveyk.orderflow.shared.inbox.adapter.InboxEventAdapter;
import com.deveyk.orderflow.shared.inbox.cleanup.InboxCleanupJob;
import com.deveyk.orderflow.shared.inbox.jpa.repository.InboxEventRepository;
import com.deveyk.orderflow.shared.inbox.port.InboxEventPort;
import com.deveyk.orderflow.shared.inbox.support.InboxProcessor;
import com.deveyk.orderflow.shared.observability.ObservabilityAutoConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
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

import java.time.Clock;

@AutoConfiguration(
        before = {HibernateJpaAutoConfiguration.class, DataJpaRepositoriesAutoConfiguration.class},
        after = ObservabilityAutoConfiguration.class)
@AutoConfigurationPackage(basePackages = "com.deveyk.orderflow.shared.inbox")
@EnableConfigurationProperties(InboxProperties.class)
public class InboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    InboxEventPort inboxEventPort(final InboxEventRepository inboxEventRepository,
                                  final ObjectProvider<Clock> clock) {

        return new InboxEventAdapter(inboxEventRepository, clock.getIfAvailable(Clock::systemUTC));
    }

    @Bean
    @ConditionalOnMissingBean
    InboxProcessor inboxProcessor(final InboxEventPort inboxEventPort,
                                  final ObjectProvider<MeterRegistry> meterRegistry) {

        return new InboxProcessor(inboxEventPort, meterRegistry.getIfAvailable(() -> Metrics.globalRegistry));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(prefix = "orderflow.inbox.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class InboxCleanupConfiguration {

        @Bean
        InboxCleanupJob inboxCleanupJob(final InboxEventRepository inboxEventRepository,
                                        final ObjectProvider<Clock> clock,
                                        final InboxProperties properties) {

            return new InboxCleanupJob(inboxEventRepository, clock.getIfAvailable(Clock::systemUTC), properties.cleanup().retention());
        }
    }
}
