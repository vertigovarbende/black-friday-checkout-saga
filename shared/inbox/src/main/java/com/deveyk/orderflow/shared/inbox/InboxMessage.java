package com.deveyk.orderflow.shared.inbox;

import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;

import java.util.UUID;


public record InboxMessage(String consumer,
                           UUID eventId,
                           String eventType,
                           String aggregateId,
                           String correlationId) {

    public InboxMessage {

        if (consumer == null || consumer.isBlank()) {
            throw new IllegalArgumentException("Inbox consumer is required");
        }

        if (eventId == null) {
            throw new IllegalArgumentException("Inbox eventId is required");
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Inbox eventType is required");
        }

        if (aggregateId == null || aggregateId.isBlank()) {
            throw new IllegalArgumentException("Inbox aggregateId is required");
        }
    }

    public static InboxMessage of(final String consumer, final EventMetadata metadata) {
        return new InboxMessage(consumer, metadata.eventId(), metadata.eventType(), metadata.aggregateId(), metadata.correlationId());
    }
}
