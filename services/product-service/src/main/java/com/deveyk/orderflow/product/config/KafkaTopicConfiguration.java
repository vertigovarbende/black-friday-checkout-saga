package com.deveyk.orderflow.product.config;

import com.deveyk.orderflow.shared.kafka.KafkaTopicProperties;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Topic'i event'i üreten, DLT'yi tüketen servis açar (event-catalog.md §4): product-service'in ürettiği dört topic ve
// tükettiği üç topic'in DLT'leri. Tükettiği topic'leri orchestrator (adım 5) ve order-service (adım 6) açar.
@Configuration(proxyBeanMethods = false)
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic productChangedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.PRODUCT_CHANGED);
    }

    @Bean
    public NewTopic stockReservedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.STOCK_RESERVED);
    }

    @Bean
    public NewTopic stockReserveFailedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.STOCK_RESERVE_FAILED);
    }

    @Bean
    public NewTopic stockReleasedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.STOCK_RELEASED);
    }

    @Bean
    public NewTopic stockReserveRequestedDeadLetterTopic(final KafkaTopicProperties properties,
                                                         final KafkaProperties kafkaProperties) {
        return properties.newTopic(TopicNames.deadLetter(TopicNames.STOCK_RESERVE_REQUESTED, kafkaProperties.getConsumer().getGroupId()));
    }

    @Bean
    public NewTopic stockReleaseRequestedDeadLetterTopic(final KafkaTopicProperties properties,
                                                         final KafkaProperties kafkaProperties) {
        return properties.newTopic(TopicNames.deadLetter(TopicNames.STOCK_RELEASE_REQUESTED, kafkaProperties.getConsumer().getGroupId()));
    }

    @Bean
    public NewTopic orderCompletedDeadLetterTopic(final KafkaTopicProperties properties,
                                                  final KafkaProperties kafkaProperties) {
        return properties.newTopic(TopicNames.deadLetter(TopicNames.ORDER_COMPLETED, kafkaProperties.getConsumer().getGroupId()));
    }
}
