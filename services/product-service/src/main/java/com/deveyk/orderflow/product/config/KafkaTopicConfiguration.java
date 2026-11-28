package com.deveyk.orderflow.product.config;

import com.deveyk.orderflow.shared.kafka.KafkaTopicProperties;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// product-service'in ürettiği event'lerin topic'leri (event-catalog.md §4). Topic'i event'i üreten, DLT'yi tüketen servis
// açar; tükettiği komutların DLT'leri listener'larla birlikte gelir (adım 3b-2).
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
}
