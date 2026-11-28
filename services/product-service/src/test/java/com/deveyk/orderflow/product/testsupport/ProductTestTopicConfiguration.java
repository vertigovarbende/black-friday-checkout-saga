package com.deveyk.orderflow.product.testsupport;

import com.deveyk.orderflow.shared.kafka.KafkaTopicProperties;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

// product-service'in tükettiği ama açmadığı topic'ler: sahipleri orchestrator (adım 5) ve order-service (adım 6).
// Testlerin broker'ında otomatik oluşturma kapalı; o servisler gelene kadar testler burada açar.
@TestConfiguration(proxyBeanMethods = false)
public class ProductTestTopicConfiguration {

    @Bean
    public NewTopic stockReserveRequestedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.STOCK_RESERVE_REQUESTED);
    }

    @Bean
    public NewTopic stockReleaseRequestedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.STOCK_RELEASE_REQUESTED);
    }

    @Bean
    public NewTopic orderCompletedTopic(final KafkaTopicProperties properties) {
        return properties.newTopic(TopicNames.ORDER_COMPLETED);
    }
}
