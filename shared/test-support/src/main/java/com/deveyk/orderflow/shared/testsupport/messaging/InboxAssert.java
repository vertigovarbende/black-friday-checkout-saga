package com.deveyk.orderflow.shared.testsupport.messaging;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Inbox tablosunu doğrudan okur (07 §9.1). Satırın varlığı "bu consumer mesajı işledi ve commit etti" demektir.
public final class InboxAssert {

    private static final String SELECT = """
            SELECT consumer, event_id, event_type, aggregate_id, processed_at
            FROM inbox_event
            WHERE consumer = ? AND event_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private InboxAssert(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static InboxAssert of(final JdbcTemplate jdbcTemplate) {
        return new InboxAssert(jdbcTemplate);
    }

    // Anahtar (consumer, event_id) olduğu için satır en fazla bir tanedir; ayrıntısını test kontrol eder.
    public InboxRow processedOnce(final String consumer, final UUID eventId) {
        final List<InboxRow> rows = rowsOf(consumer, eventId);
        assertThat(rows)
                .as("inbox rows of consumer %s for event %s", consumer, eventId)
                .hasSize(1);
        return rows.getFirst();
    }

    public void notProcessed(final String consumer, final UUID eventId) {
        assertThat(rowsOf(consumer, eventId))
                .as("inbox rows of consumer %s for event %s", consumer, eventId)
                .isEmpty();
    }

    private List<InboxRow> rowsOf(final String consumer, final UUID eventId) {
        return jdbcTemplate.query(SELECT, InboxAssert::toRow, consumer, eventId);
    }

    private static InboxRow toRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new InboxRow(
                rs.getString("consumer"),
                rs.getObject("event_id", UUID.class),
                rs.getString("event_type"),
                rs.getString("aggregate_id"),
                rs.getTimestamp("processed_at").toInstant());
    }

    public record InboxRow(String consumer, UUID eventId, String eventType, String aggregateId, Instant processedAt) {
    }
}
