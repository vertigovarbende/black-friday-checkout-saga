package com.deveyk.orderflow.order.config;

import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Topic'i event'i üreten servis açar. Otomatik oluşturma kapalı; KafkaAdmin açılışta yoksa oluşturur.
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic orderCreatedTopic(final KafkaTopicProperties properties) {
        return TopicBuilder.name(TopicNames.ORDER_CREATED)
                .partitions(properties.partitions())
                .replicas(properties.replicationFactor())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(properties.minInsyncReplicas()))
                .build();
    }
}
