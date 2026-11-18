package com.deveyk.orderflow.experiments.kafka.group;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class GroupTopics {

    public static final String GROUP = "exp.group";
    public static final String SLOW = "exp.slow";

    @Bean
    NewTopic group() {
        return TopicBuilder.name(GROUP)
                .partitions(6)
                .replicas(3)
                .build();
    }

    @Bean
    NewTopic slow() {
        return TopicBuilder.name(SLOW)
                .partitions(1)
                .replicas(3)
                .build();
    }
}
