package com.deveyk.orderflow.order.config;

import com.deveyk.orderflow.shared.messaging.TopicNames;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Topic'i event'i üreten servis açar. Otomatik oluşturma kapalı; KafkaAdmin açılışta yoksa oluşturur.
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic orderCreatedTopic(final KafkaTopicProperties properties) {
        return topic(TopicNames.ORDER_CREATED, properties);
    }

    // Geçici: product.changed'in sahibi product-service'tir (Faz 3); o gelene kadar topic'i burada açıyoruz.
    @Bean
    public NewTopic productChangedTopic(final KafkaTopicProperties properties) {
        return topic(TopicNames.PRODUCT_CHANGED, properties);
    }

    // DLT'yi tüketen servis açar: adında kendi group'u vardır. Partition sayısı kaynak topic'inkiyle aynı,
    // çünkü mesaj DLT'de de aynı partition'a yazılır (KafkaErrorHandlingConfiguration).
    @Bean
    public NewTopic productChangedDeadLetterTopic(final KafkaTopicProperties properties,
                                                  final KafkaProperties kafkaProperties) {
        return topic(TopicNames.deadLetter(TopicNames.PRODUCT_CHANGED, kafkaProperties.getConsumer().getGroupId()), properties);
    }

    private static NewTopic topic(final String name, final KafkaTopicProperties properties) {
        return TopicBuilder.name(name)
                .partitions(properties.partitions())
                .replicas(properties.replicationFactor())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(properties.minInsyncReplicas()))
                .build();
    }
}
