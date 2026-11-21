package com.deveyk.orderflow.shared.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("orderflow.outbox")
public record OutboxProperties(@DefaultValue Cleanup cleanup) {

    public record Cleanup(@DefaultValue("true") boolean enabled,
                          @DefaultValue("7d") Duration retention,
                          @DefaultValue("1h") Duration interval) {

    }
}
