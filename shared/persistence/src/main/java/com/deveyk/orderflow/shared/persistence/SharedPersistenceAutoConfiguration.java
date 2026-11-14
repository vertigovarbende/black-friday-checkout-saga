package com.deveyk.orderflow.shared.persistence;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.util.Optional;

@AutoConfiguration
@EnableJpaAuditing(dateTimeProviderRef = SharedPersistenceAutoConfiguration.DATE_TIME_PROVIDER)
public class SharedPersistenceAutoConfiguration {

    static final String DATE_TIME_PROVIDER = "orderflowAuditingDateTimeProvider";

    @Bean(DATE_TIME_PROVIDER)
    public DateTimeProvider orderflowAuditingDateTimeProvider(final ObjectProvider<Clock> clock) {
        final Clock auditingClock = clock.getIfAvailable(Clock::systemUTC);
        return () -> Optional.of(auditingClock.instant());
    }
}
