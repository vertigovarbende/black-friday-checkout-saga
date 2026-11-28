package com.deveyk.orderflow.shared.messaging;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.observability.MdcKeys;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import org.slf4j.MDC;

import java.time.Clock;
import java.util.Optional;

public class EventBuilder {

    static final String SCHEMA_VERSION = "1";

    private final TraceContextProvider traceContextProvider;
    private final Clock clock;

    public EventBuilder(final TraceContextProvider traceContextProvider, final Clock clock) {
        this.traceContextProvider = traceContextProvider;
        this.clock = clock;
    }

    public <P> Draft<P> newEvent(final P payload) {
        return new Draft<>(payload);
    }

    public final class Draft<P> {

        private final P payload;
        private String eventType;
        private String aggregateType;
        private String aggregateId;
        private String source;

        private Draft(final P payload) {
            this.payload = payload;
        }

        public Draft<P> withEventType(final String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Draft<P> withAggregate(final String aggregateType, final String aggregateId) {
            this.aggregateType = aggregateType;
            this.aggregateId = aggregateId;
            return this;
        }

        public Draft<P> withSource(final String source) {
            this.source = source;
            return this;
        }

        public OrderflowEvent<P> build() {

            if (payload == null) {
                throw new IllegalStateException("Event payload is required");
            }

            requireText(eventType, "eventType");
            requireText(aggregateType, "aggregateType");
            requireText(aggregateId, "aggregateId");
            requireText(source, "source");

            final String traceId = traceContextProvider.currentTraceId().orElse(null);
            final String correlationId = Optional.ofNullable(MDC.get(MdcKeys.CORRELATION_ID)).orElse(traceId);

            final EventMetadata metadata = new EventMetadata(UuidV7.next(), eventType, aggregateType, aggregateId, source, SCHEMA_VERSION, clock.instant(), traceId, correlationId);

            return new OrderflowEvent<>(metadata, payload);
        }

        private static void requireText(final String value, final String field) {
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Event " + field + " is required");
            }
        }
    }
}
