package com.deveyk.orderflow.shared.testsupport.messaging;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MessageProbe implements AutoCloseable {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL = Duration.ofMillis(200);

    private final KafkaConsumer<String, String> consumer;
    private final List<ConsumerRecord<String, String>> received = new ArrayList<>();

    private MessageProbe(final KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    public static MessageProbe on(final String bootstrapServers, final String topic) {

        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "message-probe-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class));
        consumer.subscribe(List.of(topic));

        return new MessageProbe(consumer);
    }

    public ConsumerRecord<String, String> awaitMessage(final String key) {
        return awaitMessages(key, 1).getFirst();
    }

    // Bu key'le gelen ilk count mesaj, topic'ten okundukları sırayla.
    public List<ConsumerRecord<String, String>> awaitMessages(final String key, final int count) {

        Awaitility.await("%d message(s) with key %s".formatted(count, key))
                .atMost(TIMEOUT)
                .pollInSameThread()          // KafkaConsumer thread-safe değil
                .until(() -> {
                    consumer.poll(POLL).forEach(received::add);
                    return withKey(key).size() >= count;
                });

        return withKey(key).subList(0, count);
    }

    public static String header(final ConsumerRecord<String, String> record, final String name) {
        final Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        consumer.close();
    }

    private List<ConsumerRecord<String, String>> withKey(final String key) {
        return received.stream()
                .filter(record -> key.equals(record.key()))
                .toList();
    }
}
