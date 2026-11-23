package com.deveyk.orderflow.shared.outbox.cleanup;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.outbox.SharedOutboxTestApplication;
import com.deveyk.orderflow.shared.outbox.testsupport.SharedOutboxTestContainerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxCleanupJobIT extends SharedOutboxTestContainerConfiguration {

    @Autowired
    private OutboxCleanupJob outboxCleanupJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanOutbox() {
        jdbcTemplate.execute("TRUNCATE TABLE outbox_event");
    }

    @Test
    @DisplayName("Saklama süresinden (varsayılan 7 gün) eski satırlar silinir, yenilere dokunulmaz")
    void givenOldAndRecentRows_whenCleanup_thenOnlyOldRowsDeleted() {
        // Given
        final Instant now = SharedOutboxTestApplication.NOW;
        final UUID old = insertRow(now.minus(Duration.ofDays(8)));
        final UUID boundary = insertRow(now.minus(Duration.ofDays(7)));
        final UUID recent = insertRow(now.minus(Duration.ofHours(1)));

        // When
        final int deleted = outboxCleanupJob.cleanup();

        // Then
        assertThat(deleted).isEqualTo(1);
        assertThat(jdbcTemplate.queryForList("SELECT id FROM outbox_event", UUID.class))
                .containsExactlyInAnyOrder(boundary, recent)
                .doesNotContain(old);
    }

    @Test
    @DisplayName("Silinecek satır yoksa sıfır döner")
    void givenOnlyRecentRows_whenCleanup_thenNothingDeleted() {
        // Given
        insertRow(SharedOutboxTestApplication.NOW);

        // When
        final int deleted = outboxCleanupJob.cleanup();

        // Then
        assertThat(deleted).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_event", Integer.class)).isEqualTo(1);
    }

    private UUID insertRow(final Instant createdAt) {
        final UUID id = UuidV7.next();
        jdbcTemplate.update("""
                INSERT INTO outbox_event (id, aggregate_type, aggregate_id, event_type, topic, payload, created_at)
                VALUES (?, 'probe', 'probe-1', 'ProbeCreated', 'probe.created', '{}'::jsonb, ?)
                """, id, Timestamp.from(createdAt));
        return id;
    }
}
