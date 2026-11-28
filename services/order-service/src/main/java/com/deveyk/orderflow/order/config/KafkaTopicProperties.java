package com.deveyk.orderflow.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Compose'da 3 broker (02 §6). Testlerde tek broker olduğu için replication-factor ve min-insync-replicas 1 verilir.
@ConfigurationProperties("orderflow.kafka.topics")
public record KafkaTopicProperties(@DefaultValue("3") int partitions,
                                   @DefaultValue("3") int replicationFactor,
                                   @DefaultValue("2") int minInsyncReplicas) {
}
