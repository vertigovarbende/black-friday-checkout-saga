package com.deveyk.orderflow.experiments.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ExperimentTopics {

    public static final String ORDERS = "exp.orders";

    @Bean
    NewTopic orders() {
        return TopicBuilder.name(ORDERS)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
