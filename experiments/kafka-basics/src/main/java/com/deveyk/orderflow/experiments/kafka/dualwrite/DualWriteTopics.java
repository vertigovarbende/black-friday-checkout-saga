package com.deveyk.orderflow.experiments.kafka.dualwrite;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class DualWriteTopics {

    public static final String ORDER_CREATED = "exp.dualwrite.order-created";

    // Outbox yolunun topic'i: order-service açar, Debezium yazar.
    public static final String OUTBOX_ORDER_CREATED = "order.created";

    @Bean
    NewTopic dualWriteOrderCreated() {
        return TopicBuilder.name(ORDER_CREATED).partitions(3).replicas(3).build();
    }
}
