package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;

@Configuration
public class RetryConfiguration {

    private static final long RETRY_INTERVAL_MS = 1000;
    private static final long MAX_RETRIES = 3;

    private final ConsumerFactory<?, ?> consumerFactory;
    private final KafkaTemplate<String, Object> dltTemplate;

    public RetryConfiguration(final ConsumerFactory<?, ?> consumerFactory, final ProducerFactory<?, ?> producerFactory) {
        this.consumerFactory = consumerFactory;
        this.dltTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                new HashMap<>(producerFactory.getConfigurationProperties()),
                new StringSerializer(),
                new DltValueSerializer()));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderMessage> retryContainerFactory() {
        return containerFactory(new ErrorHandlingDeserializer<>(new OrderMessageDeserializer()), RetryTopics.RETRY_GROUP);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderMessage> poisonContainerFactory(
            @Value("${experiments.poison.safe-deserializer:true}") final boolean safeDeserializer) {

        final Deserializer<OrderMessage> valueDeserializer = safeDeserializer
                ? new ErrorHandlingDeserializer<>(new OrderMessageDeserializer())
                : new OrderMessageDeserializer();

        return containerFactory(valueDeserializer, RetryTopics.POISON_GROUP);
    }

    private ConcurrentKafkaListenerContainerFactory<String, OrderMessage> containerFactory(
            final Deserializer<OrderMessage> valueDeserializer, final String group) {

        final ConcurrentKafkaListenerContainerFactory<String, OrderMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                new HashMap<>(consumerFactory.getConfigurationProperties()),
                new StringDeserializer(),
                valueDeserializer));
        factory.setCommonErrorHandler(errorHandler(group));

        return factory;
    }

    private DefaultErrorHandler errorHandler(final String group) {
        final DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(dltTemplate,
                (record, exception) -> new TopicPartition(record.topic() + "." + group + ".DLT", record.partition()));

        final DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        handler.addNotRetryableExceptions(ValidationFailure.class);

        return handler;
    }
}
