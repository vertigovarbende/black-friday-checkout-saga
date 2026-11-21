package com.deveyk.orderflow.shared.outbox;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@SpringBootApplication
class SharedOutboxTestApplication {

    static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    @Bean
    Clock clock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }
}
