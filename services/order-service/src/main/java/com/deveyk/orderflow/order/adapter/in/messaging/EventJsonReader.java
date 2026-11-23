package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

// Kafka'dan gelen zarfı (OrderflowEvent<P>) okur. Okunamayan ya da kimliği eksik zarf InvalidEventException olur:
// tekrar denemek sonucu değiştirmez. İkinci servis listener yazınca shared-messaging'e taşınır (01 §3.3 kural 2).
@Component
@RequiredArgsConstructor
class EventJsonReader {

    private final JsonMapper jsonMapper;

    <P> OrderflowEvent<P> read(final String json, final Class<P> payloadType) {

        if (json == null || json.isBlank()) {
            throw new InvalidEventException(MessagingErrorCode.UNREADABLE_EVENT, "empty message");
        }

        final JavaType eventType = jsonMapper.getTypeFactory().constructParametricType(OrderflowEvent.class, payloadType);
        final OrderflowEvent<P> event;
        try {

            event = jsonMapper.readerFor(eventType)
                    .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(json);

        } catch (final JacksonException e) {
            throw new InvalidEventException(MessagingErrorCode.UNREADABLE_EVENT, e, e.getOriginalMessage());
        }

        requireIdentity(event.metadata());

        if (event.payload() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload");
        }

        return event;
    }

    // Inbox'ın ihtiyaç duyduğu alanlar; diğerleri (source, traceId …) iz bilgisidir, eksikliği işlemeyi engellemez.
    private static void requireIdentity(final EventMetadata metadata) {

        if (metadata == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "metadata");
        }

        if (metadata.eventId() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "metadata.eventId");
        }

        if (metadata.eventType() == null || metadata.eventType().isBlank()) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "metadata.eventType");
        }

        if (metadata.aggregateId() == null || metadata.aggregateId().isBlank()) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "metadata.aggregateId");
        }
    }
}
