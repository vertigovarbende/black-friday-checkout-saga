package com.deveyk.orderflow.experiments.kafka.dualwrite;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

// Topic'i baştan sona okur ve DB'deki siparişlerle karşılaştırır: kayıp, hayalet ve tekrar eden event'ler.
@Component
public class DualWriteCheck {

    public static final String DIRECT = "direct";
    public static final String OUTBOX = "outbox";

    private static final Duration POLL = Duration.ofMillis(500);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final JdbcClient experimentsJdbcClient;
    private final JdbcClient orderServiceJdbcClient;
    private final Map<String, Object> consumerConfig;

    public DualWriteCheck(final JdbcClient experimentsJdbcClient,
                          final ConsumerFactory<?, ?> consumerFactory,
                          @Value("${experiments.dualwrite.outbox-db.url}") final String outboxDbUrl,
                          @Value("${experiments.dualwrite.outbox-db.username}") final String outboxDbUsername,
                          @Value("${experiments.dualwrite.outbox-db.password}") final String outboxDbPassword) {

        this.experimentsJdbcClient = experimentsJdbcClient;
        // Bean değil: ikinci bir DataSource / JdbcClient bean'i Boot'un varsayılanlarını geri çekerdi. Havuz yok, kontrol seyrek.
        this.orderServiceJdbcClient = JdbcClient.create(new DriverManagerDataSource(outboxDbUrl, outboxDbUsername, outboxDbPassword));

        final Map<String, Object> config = new HashMap<>(consumerFactory.getConfigurationProperties());
        config.remove(ConsumerConfig.GROUP_ID_CONFIG);   // assign ile okunur; group'a katılmaz, offset commit etmez
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        this.consumerConfig = config;
    }

    public Map<String, Object> check(final String path, final String run) {

        final Set<String> ordersInDb;
        final List<ConsumerRecord<String, String>> events;

        if (DIRECT.equals(path)) {
            ordersInDb = ids(experimentsJdbcClient.sql("SELECT id FROM dualwrite_order WHERE run = :run").param("run", run));
            events = readAll(DualWriteTopics.ORDER_CREATED).stream()
                    .filter(event -> run.equals(header(event, DualWriteOrders.RUN_HEADER)))
                    .toList();
        } else if (OUTBOX.equals(path)) {
            ordersInDb = ids(orderServiceJdbcClient.sql("SELECT id FROM orders"));
            events = readAll(DualWriteTopics.OUTBOX_ORDER_CREATED);
        } else {
            throw new IllegalArgumentException("path must be one of [direct, outbox]");
        }

        final Map<String, Long> eventsByOrder = events.stream()
                .collect(Collectors.groupingBy(ConsumerRecord::key, TreeMap::new, Collectors.counting()));

        final Set<String> missing = new TreeSet<>(ordersInDb);
        missing.removeAll(eventsByOrder.keySet());

        final Set<String> ghost = new TreeSet<>(eventsByOrder.keySet());
        ghost.removeAll(ordersInDb);

        final Map<String, Long> duplicates = new TreeMap<>(eventsByOrder);
        duplicates.values().removeIf(count -> count < 2);

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("path", path);
        if (DIRECT.equals(path)) {
            result.put("run", run);
        }
        result.put("ordersInDb", ordersInDb.size());
        result.put("eventsInTopic", events.size());
        result.put("missingEvents", missing);
        result.put("ghostEvents", ghost);
        result.put("duplicateEvents", duplicates);

        return result;
    }

    private static Set<String> ids(final JdbcClient.StatementSpec statement) {
        return statement.query((resultSet, rowNum) -> resultSet.getString("id")).set();
    }

    private List<ConsumerRecord<String, String>> readAll(final String topic) {

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig, new StringDeserializer(), new StringDeserializer())) {

            final List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(info -> new TopicPartition(topic, info.partition()))
                    .toList();
            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);
            final Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

            final List<ConsumerRecord<String, String>> records = new ArrayList<>();
            final Instant deadline = Instant.now().plus(TIMEOUT);

            while (partitions.stream().anyMatch(partition -> consumer.position(partition) < endOffsets.get(partition))) {
                if (Instant.now().isAfter(deadline)) {
                    throw new IllegalStateException("Could not read " + topic + " to the end within " + TIMEOUT);
                }
                consumer.poll(POLL).forEach(records::add);
            }

            return records;
        }
    }

    private static String header(final ConsumerRecord<String, String> record, final String name) {
        final Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
