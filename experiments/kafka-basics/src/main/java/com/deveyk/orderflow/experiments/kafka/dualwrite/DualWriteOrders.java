package com.deveyk.orderflow.experiments.kafka.dualwrite;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Siparişi DB'ye yazıp event'i KafkaTemplate ile doğrudan gönderir: outbox'sız "dual write". Arızalar bilerek verilir.
@Component
public class DualWriteOrders {

    public static final String COMMIT_THEN_SEND = "commit-then-send";
    public static final String SEND_THEN_COMMIT = "send-then-commit";

    public static final String NONE = "none";
    public static final String CRASH_BEFORE_SEND = "crash-before-send";
    public static final String CRASH_AFTER_SEND = "crash-after-send";
    public static final String ROLLBACK_AFTER_SEND = "rollback-after-send";

    public static final String RUN_HEADER = "run";

    // 6.6: ack beklenmeyen gönderim tamponda 2 sn kalır; halt'ın mesajı tamponda yakalaması kesinleşsin
    // (Kafka 4'te varsayılan linger.ms 5 ms; sonuç şansa kalırdı).
    static final int LINGER_MS = 2000;

    private static final Logger log = LoggerFactory.getLogger(DualWriteOrders.class);
    private static final Set<String> FLOWS = Set.of(COMMIT_THEN_SEND, SEND_THEN_COMMIT);
    private static final Set<String> FAULTS = Set.of(NONE, CRASH_BEFORE_SEND, CRASH_AFTER_SEND, ROLLBACK_AFTER_SEND);

    private final JdbcClient jdbcClient;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, String> lingeringKafkaTemplate;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public DualWriteOrders(final JdbcClient jdbcClient,
                           final PlatformTransactionManager transactionManager,
                           final KafkaTemplate<String, String> kafkaTemplate,
                           final ProducerFactory<?, ?> producerFactory) {

        this.jdbcClient = jdbcClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.kafkaTemplate = kafkaTemplate;

        // Bean değil: KafkaTemplate bean'i tanımlansaydı Boot'un varsayılan template'i (diğer deneylerin kullandığı) geri çekilirdi.
        final Map<String, Object> config = new HashMap<>(producerFactory.getConfigurationProperties());
        config.put(ProducerConfig.LINGER_MS_CONFIG, LINGER_MS);
        this.lingeringKafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(config, new StringSerializer(), new StringSerializer()));
    }

    public Map<String, Object> place(final String flow, final String fault, final boolean ack, final String run) {

        validate(flow, fault);

        final UUID orderId = UUID.randomUUID();
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("flow", flow);
        result.put("fault", fault);
        result.put("ack", ack);
        result.put("run", run);

        if (COMMIT_THEN_SEND.equals(flow)) {
            transactionTemplate.executeWithoutResult(status -> insert(orderId, run));
            log.info("Order committed orderId={}", orderId);
            crashIf(fault, CRASH_BEFORE_SEND, orderId);
            send(orderId, run, ack, result);
            crashIf(fault, CRASH_AFTER_SEND, orderId);
        } else {
            transactionTemplate.executeWithoutResult(status -> {
                insert(orderId, run);
                crashIf(fault, CRASH_BEFORE_SEND, orderId);
                send(orderId, run, ack, result);
                crashIf(fault, CRASH_AFTER_SEND, orderId);
                if (ROLLBACK_AFTER_SEND.equals(fault)) {
                    log.warn("Rolling back after send orderId={}", orderId);
                    throw new IllegalStateException("Simulated failure after send, transaction rolls back: " + orderId);
                }
            });
            log.info("Order committed orderId={}", orderId);
        }

        return result;
    }

    private void insert(final UUID orderId, final String run) {
        jdbcClient.sql("INSERT INTO dualwrite_order (id, run) VALUES (:id, :run)")
                .param("id", orderId)
                .param("run", run)
                .update();
    }

    private void send(final UUID orderId, final String run, final boolean ack, final Map<String, Object> result) {

        final ProducerRecord<String, String> record = new ProducerRecord<>(DualWriteTopics.ORDER_CREATED,
                orderId.toString(), mapper.writeValueAsString(Map.of("orderId", orderId, "run", run)));
        record.headers().add(RUN_HEADER, run.getBytes(StandardCharsets.UTF_8));

        if (!ack) {
            // send() mesajı producer'ın tamponuna koyar ve döner; Kafka'ya ne zaman gideceği linger.ms'e bağlı.
            lingeringKafkaTemplate.send(record);
            log.info("Event handed to producer, not acked orderId={}", orderId);
            return;
        }

        try {
            final RecordMetadata metadata = kafkaTemplate.send(record).get(10, TimeUnit.SECONDS).getRecordMetadata();
            result.put("partition", metadata.partition());
            result.put("offset", metadata.offset());
            log.info("Event acked orderId={} partition={} offset={}", orderId, metadata.partition(), metadata.offset());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while sending " + orderId, e);
        } catch (final ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Send failed for " + orderId, e);
        }
    }

    // Gerçek çökme: halt ne finally bloklarını ne de producer'ın close()/flush'ını çalıştırır.
    private static void crashIf(final String fault, final String point, final UUID orderId) {
        if (point.equals(fault)) {
            log.warn("Crashing at {} orderId={} (Runtime.halt)", point, orderId);
            Runtime.getRuntime().halt(1);
        }
    }

    private static void validate(final String flow, final String fault) {
        if (!FLOWS.contains(flow)) {
            throw new IllegalArgumentException("flow must be one of " + FLOWS);
        }
        if (!FAULTS.contains(fault)) {
            throw new IllegalArgumentException("fault must be one of " + FAULTS);
        }
        if (COMMIT_THEN_SEND.equals(flow) && ROLLBACK_AFTER_SEND.equals(fault)) {
            throw new IllegalArgumentException("rollback-after-send needs send-then-commit: with commit-then-send the order is already committed");
        }
    }
}
