package com.deveyk.orderflow.shared.testsupport.messaging;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Outbox tablosunu doğrudan okur (07 §9.1). Debezium'suz testlerde "event yazıldı mı?" sorusunun cevabı.
public final class OutboxAssert {

    private static final String SELECT = """
            SELECT id, aggregate_type, aggregate_id, event_type, topic, payload::text AS payload, traceparent, created_at
            FROM outbox_event
            WHERE aggregate_id = ?
            ORDER BY created_at, id
            """;

    private final JdbcTemplate jdbcTemplate;

    private OutboxAssert(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static OutboxAssert of(final JdbcTemplate jdbcTemplate) {
        return new OutboxAssert(jdbcTemplate);
    }

    // Aggregate için bu tipte tam bir event olmalı; satırı döner, ayrıntısını test kontrol eder.
    public OutboxRow hasEvent(final String aggregateId, final String eventType) {
        final List<OutboxRow> rows = rowsOf(aggregateId).stream()
                .filter(row -> row.eventType().equals(eventType))
                .toList();
        assertThat(rows)
                .as("outbox events of type %s for aggregate %s", eventType, aggregateId)
                .hasSize(1);
        return rows.getFirst();
    }

    // Aynı cevabın yeniden yayını gibi durumlar için: bu tipte tam count kadar event.
    public List<OutboxRow> hasEvents(final String aggregateId, final String eventType, final int count) {
        final List<OutboxRow> rows = rowsOf(aggregateId).stream()
                .filter(row -> row.eventType().equals(eventType))
                .toList();
        assertThat(rows)
                .as("outbox events of type %s for aggregate %s", eventType, aggregateId)
                .hasSize(count);
        return rows;
    }

    public void hasNoEvent(final String aggregateId) {
        assertThat(rowsOf(aggregateId))
                .as("outbox events for aggregate %s", aggregateId)
                .isEmpty();
    }

    private List<OutboxRow> rowsOf(final String aggregateId) {
        return jdbcTemplate.query(SELECT, OutboxAssert::toRow, aggregateId);
    }

    private static OutboxRow toRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new OutboxRow(
                rs.getObject("id", UUID.class),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("topic"),
                rs.getString("payload"),
                rs.getString("traceparent"),
                rs.getTimestamp("created_at").toInstant());
    }

    public record OutboxRow(UUID id, String aggregateType, String aggregateId, String eventType, String topic,
                            String payload, String traceparent, Instant createdAt) {
    }
}
