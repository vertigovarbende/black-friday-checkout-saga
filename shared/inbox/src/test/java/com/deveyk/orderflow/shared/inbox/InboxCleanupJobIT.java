package com.deveyk.orderflow.shared.inbox;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.inbox.testsupport.SharedInboxTestContainerConfiguration;
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

class InboxCleanupJobIT extends SharedInboxTestContainerConfiguration {

    @Autowired
    private InboxCleanupJob inboxCleanupJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanInbox() {
        jdbcTemplate.execute("TRUNCATE TABLE inbox_event");
    }

    @Test
    @DisplayName("Saklama süresinden (varsayılan 7 gün) eski kayıtlar silinir, yenilere dokunulmaz")
    void givenOldAndRecentRows_whenCleanup_thenOnlyOldRowsDeleted() {
        // Given
        final Instant now = SharedInboxTestApplication.NOW;
        final UUID old = insertRow(now.minus(Duration.ofDays(8)));
        final UUID boundary = insertRow(now.minus(Duration.ofDays(7)));
        final UUID recent = insertRow(now.minus(Duration.ofHours(1)));

        // When
        final int deleted = inboxCleanupJob.cleanup();

        // Then
        assertThat(deleted).isEqualTo(1);
        assertThat(jdbcTemplate.queryForList("SELECT event_id FROM inbox_event", UUID.class))
                .containsExactlyInAnyOrder(boundary, recent)
                .doesNotContain(old);
    }

    @Test
    @DisplayName("Silinecek kayıt yoksa sıfır döner")
    void givenOnlyRecentRows_whenCleanup_thenNothingDeleted() {
        // Given
        insertRow(SharedInboxTestApplication.NOW);

        // When
        final int deleted = inboxCleanupJob.cleanup();

        // Then
        assertThat(deleted).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM inbox_event", Integer.class)).isEqualTo(1);
    }

    private UUID insertRow(final Instant processedAt) {
        final UUID eventId = UuidV7.next();
        jdbcTemplate.update("""
                INSERT INTO inbox_event (consumer, event_id, event_type, aggregate_id, processed_at)
                VALUES ('order-service', ?, 'ProbeChanged', 'probe-1', ?)
                """, eventId, Timestamp.from(processedAt));
        return eventId;
    }
}
