package com.deveyk.orderflow.shared.persistence;

import com.deveyk.orderflow.shared.persistence.testsupport.MutableClock;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

/**
 * Sadece testte var. {@code SharedPersistenceAutoConfiguration} component scan ile değil,
 * {@code AutoConfiguration.imports} ile gelir; IT bunu da doğrulamış olur.
 */
@SpringBootApplication
class SharedPersistenceTestApplication {

    static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");   // Black Friday

    @Bean
    MutableClock clock() {
        return MutableClock.at(NOW);
    }
}
