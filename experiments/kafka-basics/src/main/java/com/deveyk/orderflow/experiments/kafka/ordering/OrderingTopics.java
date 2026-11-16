package com.deveyk.orderflow.experiments.kafka.ordering;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class OrderingTopics {

    public static final String CREATED = "exp.order.created";
    public static final String CANCELLED = "exp.order.cancelled";
    public static final String HOT = "exp.hot";

    @Bean
    NewTopic orderCreated() {
        return TopicBuilder.name(CREATED)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic orderCancelled() {
        return TopicBuilder.name(CANCELLED)
                .partitions(3)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic hot() {
        return TopicBuilder.name(HOT)
                .partitions(3)
                .replicas(3)
                .build();
    }
}
