package com.deveyk.orderflow.shared.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "inbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InboxEventEntity {

    @EmbeddedId
    private InboxEventKey id;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 128)
    private String aggregateId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

}
