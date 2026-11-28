package com.deveyk.orderflow.order.delivery;

import ch.qos.logback.classic.Level;
import com.deveyk.orderflow.order.testsupport.OrderDeliveryTestConfiguration;
import com.deveyk.orderflow.order.testsupport.builder.ProductChangedEventBuilder;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import com.deveyk.orderflow.shared.testsupport.messaging.MessageProbe;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterDeliveryIT extends OrderDeliveryTestConfiguration {

    private static final String CONSUMER = "order-service.product-changed";
    private static final String DEAD_LETTER_TOPIC = TopicNames.deadLetter(TopicNames.PRODUCT_CHANGED, "order-service");
    private static final String DLT_COUNTER = "orderflow.messaging.dlt";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Domain kuralına uymayan product.changed (boş ad) retry'sız DLT'ye gider; kopya ve inbox kaydı yazılmaz")
    void givenBlankProductName_whenConsumed_thenDeadLetteredWithoutRetryAndRolledBack() {
        // Given
        final UUID productId = UUID.randomUUID();

        final OrderflowEvent<ProductChangedPayload> event = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withName(" ")
                .build();

        final double deadLettersBefore = deadLetters("ORD_202");

        try (MessageProbe probe = messageProbe(DEAD_LETTER_TOPIC)) {

            // When
            final RecordMetadata sent = publishEvent(TopicNames.PRODUCT_CHANGED, event);

            // Then
            final ConsumerRecord<String, String> deadLetter = probe.awaitMessage(productId.toString());
            assertThat(deadLetter.partition()).as("DLT'de aynı partition").isEqualTo(sent.partition());
            assertThat(MessageProbe.header(deadLetter, "id")).isEqualTo(event.metadata().eventId().toString());
            assertThat(MessageProbe.header(deadLetter, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
                    .isEqualTo("com.deveyk.orderflow.shared.domain.exception.DomainValidationException");
            assertThat(ByteBuffer.wrap(deadLetter.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_OFFSET).value()).getLong())
                    .isEqualTo(sent.offset());

            awaitDeadLetterCounted("ORD_202", deadLettersBefore);
            final String position = "partition=" + sent.partition() + " offset=" + sent.offset();
            assertThat(logTracker.findMessage(Level.WARN, position + " attempt=1 code=ORD_202")).isPresent();
            assertThat(logTracker.findMessage(Level.WARN, position + " attempt=2")).as("retry yok").isEmpty();
            assertThat(logTracker.findMessage(Level.ERROR, "Message sent to DLT topic=" + TopicNames.PRODUCT_CHANGED
                    + " dlt=" + DEAD_LETTER_TOPIC + " " + position)).isPresent();

            InboxAssert.of(jdbcTemplate).notProcessed(CONSUMER, event.metadata().eventId());
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM product_snapshots WHERE id = ?",
                    Integer.class, productId)).isZero();
        }
    }

    @Test
    @DisplayName("JSON olarak okunamayan mesaj (poison pill) retry'sız DLT'ye orijinal haliyle gider")
    void givenUnreadableMessage_whenConsumed_thenDeadLetteredAsIs() {
        // Given
        final String key = UUID.randomUUID().toString();
        final String poisonPill = "{not json";
        final double deadLettersBefore = deadLetters("ORD_301");

        try (MessageProbe probe = messageProbe(DEAD_LETTER_TOPIC)) {

            // When
            final RecordMetadata sent = publishRaw(TopicNames.PRODUCT_CHANGED, key, poisonPill);

            // Then
            final ConsumerRecord<String, String> deadLetter = probe.awaitMessage(key);
            assertThat(deadLetter.value()).isEqualTo(poisonPill);
            assertThat(deadLetter.partition()).isEqualTo(sent.partition());

            awaitDeadLetterCounted("ORD_301", deadLettersBefore);
            assertThat(logTracker.findMessage(Level.WARN, "partition=" + sent.partition() + " offset=" + sent.offset()
                    + " attempt=2")).as("retry yok").isEmpty();
        }
    }

    // Recoverer önce DLT'ye yazar, sonra loglar ve sayar: sayaç artınca log da yazılmıştır.
    private void awaitDeadLetterCounted(final String code, final double before) {

        Awaitility.await("DLT counter for %s".formatted(code))
                .atMost(Duration.ofSeconds(10))
                .until(() -> deadLetters(code) - before, counted -> counted == 1.0);
    }

    private double deadLetters(final String code) {

        final Counter counter = meterRegistry.find(DLT_COUNTER)
                .tags("topic", TopicNames.PRODUCT_CHANGED, "code", code)
                .counter();

        return counter == null ? 0.0 : counter.count();
    }
}
