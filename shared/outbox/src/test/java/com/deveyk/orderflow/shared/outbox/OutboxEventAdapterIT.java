package com.deveyk.orderflow.shared.outbox;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.outbox.testsupport.ProbePayload;
import com.deveyk.orderflow.shared.outbox.testsupport.SharedOutboxTestContainerConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxEventAdapterIT extends SharedOutboxTestContainerConfiguration {

    private static final String TOPIC = "probe.created";

    @Autowired
    private OutboxEventPort outboxEventPort;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanOutbox() {
        jdbcTemplate.execute("TRUNCATE TABLE outbox_event");
    }

    @Test
    @DisplayName("Event outbox'a yazılır: id = eventId, key = aggregateId, payload zarfın tamamı")
    void givenEvent_whenAppendInTransaction_thenRowWrittenFromMetadata() {
        // Given
        final OrderflowEvent<ProbePayload> event = probeEvent("probe-1");

        // When
        transactionTemplate.executeWithoutResult(status -> outboxEventPort.append(TOPIC, event));

        // Then
        final Map<String, Object> row = jdbcTemplate.queryForMap("SELECT * FROM outbox_event WHERE id = ?", event.metadata().eventId());
        assertThat(row.get("aggregate_type")).isEqualTo("probe");
        assertThat(row.get("aggregate_id")).isEqualTo("probe-1");
        assertThat(row.get("event_type")).isEqualTo("ProbeCreated");
        assertThat(row.get("topic")).isEqualTo(TOPIC);
        assertThat(row.get("traceparent")).isNull();
        assertThat(((Timestamp) row.get("created_at")).toInstant()).isEqualTo(SharedOutboxTestApplication.NOW);

        final String payload = row.get("payload").toString();
        assertThat(JsonPath.<String>read(payload, "$.metadata.eventId")).isEqualTo(event.metadata().eventId().toString());
        assertThat(JsonPath.<String>read(payload, "$.metadata.occurredAt")).isEqualTo("2026-11-27T10:00:00Z");
        assertThat(JsonPath.<String>read(payload, "$.payload.value")).isEqualTo("hello");
    }

    @Test
    @DisplayName("Aynı transaction'da hata olursa outbox satırı da geri alınır")
    void givenFailureAfterAppend_whenTransactionRollsBack_thenNoRow() {
        // Given
        final OrderflowEvent<ProbePayload> event = probeEvent("probe-2");

        // When
        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
            outboxEventPort.append(TOPIC, event);
            throw new IllegalStateException("business logic failed after append");
        })).isInstanceOf(IllegalStateException.class);

        // Then
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_event", Integer.class)).isZero();
    }

    @Test
    @DisplayName("Transaction dışında yazmaya izin verilmez; hiçbir satır oluşmaz")
    void givenNoTransaction_whenAppend_thenIllegalTransactionState() {
        // Given
        final OrderflowEvent<ProbePayload> event = probeEvent("probe-3");

        // When / Then
        assertThatThrownBy(() -> outboxEventPort.append(TOPIC, event))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_event", Integer.class)).isZero();
    }

    private static OrderflowEvent<ProbePayload> probeEvent(final String aggregateId) {
        final UUID eventId = UuidV7.next();
        final EventMetadata metadata = new EventMetadata(eventId, "ProbeCreated", "probe", aggregateId,
                "probe-service", "1", SharedOutboxTestApplication.NOW, null, null);
        return new OrderflowEvent<>(metadata, new ProbePayload("hello"));
    }
}
