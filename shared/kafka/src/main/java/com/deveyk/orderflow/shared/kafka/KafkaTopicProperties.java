package com.deveyk.orderflow.shared.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.kafka.config.TopicBuilder;

@ConfigurationProperties("orderflow.kafka.topics")
public record KafkaTopicProperties(@DefaultValue("3") int partitions,
                                   @DefaultValue("3") int replicationFactor,
                                   @DefaultValue("2") int minInsyncReplicas) {

    // Topic'i üreten, DLT'yi tüketen servis açar (event-catalog.md §1). DLT'nin partition sayısı da aynıdır:
    // mesaj DLT'de aynı partition'a yazılır.
    public NewTopic newTopic(final String name) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(minInsyncReplicas))
                .build();
    }
}
