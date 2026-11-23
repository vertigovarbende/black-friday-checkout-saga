package com.deveyk.orderflow.shared.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

interface InboxEventRepository extends JpaRepository<InboxEventEntity, InboxEventKey> {

    @Modifying
    @Query(value = """
            INSERT INTO inbox_event (consumer, event_id, event_type, aggregate_id, processed_at)
            VALUES (:consumer, :eventId, :eventType, :aggregateId, :processedAt)
            ON CONFLICT (consumer, event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("consumer") String consumer,
                       @Param("eventId") UUID eventId,
                       @Param("eventType") String eventType,
                       @Param("aggregateId") String aggregateId,
                       @Param("processedAt") Instant processedAt);

    @Modifying
    @Query("delete from InboxEventEntity e where e.processedAt < :cutoff")
    int deleteProcessedBefore(@Param("cutoff") Instant cutoff);
}
