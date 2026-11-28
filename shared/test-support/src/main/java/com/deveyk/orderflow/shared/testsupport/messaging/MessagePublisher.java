package com.deveyk.orderflow.shared.testsupport.messaging;

import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Testte bir topic'e mesaj yazar: başka bir servisin (Debezium üzerinden) yayınladığı event'i ya da bozuk bir mesajı
// taklit etmek için. MessageProbe'un karşılığı.
public final class MessagePublisher implements AutoCloseable {

    private static final long TIMEOUT_SECONDS = 30;

    // Debezium EventRouter'ın her kayda koyduğu header; değeri eventId'dir (infra/debezium/*-outbox.json).
    private static final String EVENT_ID_HEADER = "id";

    private final KafkaProducer<String, String> producer;
    private final JsonMapper jsonMapper;

    private MessagePublisher(final KafkaProducer<String, String> producer, final JsonMapper jsonMapper) {
        this.producer = producer;
        this.jsonMapper = jsonMapper;
    }

    // Zarf, servisin kendi JsonMapper'ıyla yazılır: outbox'a yazanla aynı JSON.
    public static MessagePublisher on(final String bootstrapServers, final JsonMapper jsonMapper) {

        final KafkaProducer<String, String> producer = new KafkaProducer<>(Map.<String, Object>of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class));

        return new MessagePublisher(producer, jsonMapper);
    }

    // Debezium'un yazdığı gibi: key = aggregateId, değer zarfın JSON'u, "id" header'ında eventId.
    public RecordMetadata publishEvent(final String topic, final OrderflowEvent<?> event) {
        return send(new ProducerRecord<>(topic, null, event.metadata().aggregateId(), jsonMapper.writeValueAsString(event),
                List.of(new RecordHeader(EVENT_ID_HEADER, event.metadata().eventId().toString().getBytes(StandardCharsets.UTF_8)))));
    }

    // Zarf olmayan ya da bozuk mesajlar için.
    public RecordMetadata publishRaw(final String topic, final String key, final String value) {
        return send(new ProducerRecord<>(topic, key, value));
    }

    @Override
    public void close() {
        producer.close();
    }

    private RecordMetadata send(final ProducerRecord<String, String> record) {
        try {

            return producer.send(record).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing to " + record.topic(), e);
        } catch (final ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not publish to " + record.topic(), e);
        }
    }
}
