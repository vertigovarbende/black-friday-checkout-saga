package com.deveyk.orderflow.shared.messaging.contract;

import java.time.Instant;
import java.util.UUID;

// version: payload şemasının sürümü. traceId ve correlationId tracing kapalıysa null olabilir.
public record EventMetadata(UUID eventId,
                            String eventType,
                            String aggregateType,
                            String aggregateId,
                            String source,
                            String version,
                            Instant occurredAt,
                            String traceId,
                            String correlationId
) {

}
