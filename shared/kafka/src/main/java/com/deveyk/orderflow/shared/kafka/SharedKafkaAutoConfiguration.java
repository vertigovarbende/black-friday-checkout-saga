package com.deveyk.orderflow.shared.kafka;

import com.deveyk.orderflow.shared.messaging.TopicNames;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.nio.charset.StandardCharsets;

@Slf4j
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class SharedKafkaAutoConfiguration {

    static final String DLT_COUNTER = "orderflow.messaging.dlt";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_INTERVAL_MS = 1_000;
    private static final double MULTIPLIER = 2.0;
    private static final String EVENT_ID_HEADER = "id";

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    public DefaultErrorHandler kafkaErrorHandler(final KafkaTemplate<?, ?> kafkaTemplate,
                                                 final KafkaProperties kafkaProperties,
                                                 final ObjectProvider<MeterRegistry> meterRegistryProvider) {

        final String groupId = kafkaProperties.getConsumer().getGroupId();
        final MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable(() -> Metrics.globalRegistry);

        // DLT'de aynı partition: aynı key'in mesajları orada da sıralı kalır.
        final DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(TopicNames.deadLetter(record.topic(), groupId), record.partition()));

        // Varsayılan: geçici hatalar, optimistic lock ve bilinmeyenler (06 §8).
        final ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRIES);
        backOff.setInitialInterval(INITIAL_INTERVAL_MS);
        backOff.setMultiplier(MULTIPLIER);

        final DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {

            deadLetterRecoverer.accept(record, exception);   // DLT'ye yazılamazsa fırlatır; kayıt yeniden denenir
            final String code = KafkaErrorPolicy.errorCode(exception);

            log.error("Message sent to DLT topic={} dlt={} partition={} offset={} eventId={} code={}",
                    record.topic(), TopicNames.deadLetter(record.topic(), groupId), record.partition(),
                    record.offset(), eventId(record), code, exception);
            meterRegistry.counter(DLT_COUNTER, "topic", record.topic(), "code", code).increment();

        }, backOff);

        KafkaErrorPolicy.NOT_RETRYABLE.forEach(type -> errorHandler.addNotRetryableExceptions(type));
        errorHandler.setBackOffFunction((record, exception) -> KafkaErrorPolicy.backOffFor(exception));

        // Her başarısız deneme bir WARN; DLT'ye düşüş yukarıda bir ERROR. Spring'in kendi hata satırı DEBUG'a iner (log bir kez).
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.warn("Message processing failed topic={} partition={} offset={} attempt={} code={} error={}",
                        record.topic(), record.partition(), record.offset(), deliveryAttempt,
                        KafkaErrorPolicy.errorCode(exception),
                        exception == null ? null : KafkaErrorPolicy.rootCause(exception).getMessage()));
        errorHandler.setLogLevel(KafkaException.Level.DEBUG);

        return errorHandler;
    }

    private static String eventId(final ConsumerRecord<?, ?> record) {
        final Header header = record.headers().lastHeader(EVENT_ID_HEADER);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
