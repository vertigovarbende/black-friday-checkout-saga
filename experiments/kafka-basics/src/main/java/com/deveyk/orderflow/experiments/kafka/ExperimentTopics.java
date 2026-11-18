package com.deveyk.orderflow.experiments.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ExperimentTopics {

    public static final String ORDERS = "exp.orders";
    public static final String ACKS = "exp.acks";
    public static final String ACKS_STRICT = "exp.acks.strict";

    @Bean
    NewTopic orders() {
        return TopicBuilder.name(ORDERS)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic acks() {
        return TopicBuilder.name(ACKS)
                .partitions(1)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic acksStrict() {
        return TopicBuilder.name(ACKS_STRICT)
                .partitions(1)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "3")
                .build();
    }
}
