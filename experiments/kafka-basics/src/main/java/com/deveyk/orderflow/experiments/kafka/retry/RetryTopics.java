package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class RetryTopics {

    public static final String RETRY = "exp.retry";
    public static final String RETRY_GROUP = "exp-retry";
    public static final String RETRY_DLT = RETRY + "." + RETRY_GROUP + ".DLT";

    public static final String POISON = "exp.poison";
    public static final String POISON_GROUP = "exp-poison";
    public static final String POISON_DLT = POISON + "." + POISON_GROUP + ".DLT";

    @Bean
    NewTopic retry() {
        return TopicBuilder.name(RETRY).partitions(3).replicas(3).build();
    }

    @Bean
    NewTopic retryDlt() {
        return TopicBuilder.name(RETRY_DLT).partitions(3).replicas(3).build();
    }

    @Bean
    NewTopic poison() {
        return TopicBuilder.name(POISON).partitions(1).replicas(3).build();
    }

    @Bean
    NewTopic poisonDlt() {
        return TopicBuilder.name(POISON_DLT).partitions(1).replicas(3).build();
    }
}
