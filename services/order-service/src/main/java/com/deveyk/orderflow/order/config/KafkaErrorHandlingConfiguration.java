package com.deveyk.orderflow.order.config;

import com.deveyk.orderflow.shared.domain.exception.OrderflowException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import tools.jackson.core.JacksonException;

import java.nio.charset.StandardCharsets;

// Listener'lardan çıkan hatanın tek yeri (06 §8, ADR-0010): bloklayan retry, sonra <topic>.<group>.DLT.
// Şimdilik sadece iki sınıf retry'sız; tam sınıflandırma ve shared modüle taşınması Faz 3'te.
@Slf4j
@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfiguration {

    static final String DLT_COUNTER = "orderflow.messaging.dlt";

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_INTERVAL_MS = 1_000;
    private static final double MULTIPLIER = 2.0;

    // Debezium EventRouter'ın her kayda koyduğu header; değeri eventId'dir.
    private static final String EVENT_ID_HEADER = "id";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(final KafkaTemplate<?, ?> kafkaTemplate,
                                                 final KafkaProperties kafkaProperties,
                                                 final MeterRegistry meterRegistry) {

        final String groupId = kafkaProperties.getConsumer().getGroupId();

        // DLT'de aynı partition: aynı key'in mesajları orada da sıralı kalır.
        final DeadLetterPublishingRecoverer deadLetterRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(TopicNames.deadLetter(record.topic(), groupId), record.partition()));

        final ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRIES);
        backOff.setInitialInterval(INITIAL_INTERVAL_MS);
        backOff.setMultiplier(MULTIPLIER);

        final DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {

            deadLetterRecoverer.accept(record, exception);   // DLT'ye yazılamazsa fırlatır; kayıt yeniden denenir
            final String code = errorCode(exception);

            log.error("Message sent to DLT topic={} dlt={} partition={} offset={} eventId={} code={}",
                    record.topic(), TopicNames.deadLetter(record.topic(), groupId), record.partition(),
                    record.offset(), eventId(record), code, exception);
            meterRegistry.counter(DLT_COUNTER, "topic", record.topic(), "code", code).increment();

        }, backOff);

        // Tekrar denense sonuç değişmez. Spring'in kendi listesi (DeserializationException …) da geçerli kalır.
        errorHandler.addNotRetryableExceptions(OrderflowValidationException.class, JacksonException.class);

        // Her başarısız deneme bir WARN; DLT'ye düşüş yukarıda bir ERROR. Spring'in kendi hata satırı DEBUG'a iner (log bir kez).
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.warn("Message processing failed topic={} partition={} offset={} attempt={} code={} error={}",
                        record.topic(), record.partition(), record.offset(), deliveryAttempt,
                        errorCode(exception), exception == null ? null : rootCause(exception).getMessage()));
        errorHandler.setLogLevel(KafkaException.Level.DEBUG);

        return errorHandler;
    }

    // Zincirdeki ilk OrderflowException'ın kodu (ORD_202 …); yoksa en içteki exception'ın adı. Metrik etiketi olduğu için
    // değer kümesi sınırlıdır.
    static String errorCode(final Throwable exception) {

        if (exception == null) {
            return "unknown";
        }

        for (Throwable current = exception; current != null; current = current.getCause()) {
            if (current instanceof final OrderflowException orderflowException) {
                return orderflowException.getErrorCode().code();
            }

        }

        return rootCause(exception).getClass().getSimpleName();
    }

    private static Throwable rootCause(final Throwable exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static String eventId(final ConsumerRecord<?, ?> record) {
        final Header header = record.headers().lastHeader(EVENT_ID_HEADER);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
