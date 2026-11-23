package com.deveyk.orderflow.shared.inbox;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@SpringBootApplication
public class SharedInboxTestApplication {

    public static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");

    @Bean
    Clock clock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    // Duplicate sayacını okuyabilmek için; servislerde actuator'ın registry'si kullanılır.
    @Bean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
