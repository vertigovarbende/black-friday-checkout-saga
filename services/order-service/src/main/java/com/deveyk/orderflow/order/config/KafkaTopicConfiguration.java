package com.deveyk.orderflow.order.config;

import com.deveyk.orderflow.shared.kafka.KafkaTopicProperties;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic orderCreatedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.ORDER_CREATED);
    }

    // Geçici: product.changed'in sahibi product-service'tir (Faz 3 adım 3); o gelene kadar topic'i burada açıyoruz.
    @Bean
    public NewTopic productChangedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.PRODUCT_CHANGED);
    }

    @Bean
    public NewTopic productChangedDeadLetterTopic(final KafkaTopicProperties properties,
                                                  final KafkaProperties kafkaProperties) {
        return properties.newTopic(TopicNames.deadLetter(TopicNames.PRODUCT_CHANGED, kafkaProperties.getConsumer().getGroupId()));
    }
}
