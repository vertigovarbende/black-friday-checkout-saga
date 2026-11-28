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

    public <P> Draft<P> newEvent(final EventDefinition definition, final String aggregateId, final P payload) {
        return new Draft<>(definition, aggregateId, payload);
    }

    public final class Draft<P> {

        private final EventDefinition definition;
        private final String aggregateId;
        private final P payload;
        private String source;

        private Draft(final EventDefinition definition, final String aggregateId, final P payload) {
            this.definition = definition;
            this.aggregateId = aggregateId;
            this.payload = payload;
        }

        public Draft<P> withSource(final String source) {
            this.source = source;
            return this;
        }

        public OrderflowEvent<P> build() {

            if (definition == null)
                throw new IllegalStateException("Event definition is required");

            if (payload == null)
                throw new IllegalStateException("Event payload is required");

            if (aggregateId == null || aggregateId.isBlank())
                throw new IllegalStateException("Event aggregateId is required");

            if (source == null || source.isBlank())
                throw new IllegalStateException("Event source is required");

            final String traceId = traceContextProvider.currentTraceId().orElse(null);
            final String correlationId = Optional.ofNullable(MDC.get(MdcKeys.CORRELATION_ID)).orElse(traceId);

            final EventMetadata metadata = new EventMetadata(UuidV7.next(), definition.eventType(), definition.aggregateType(), aggregateId, source, SCHEMA_VERSION, clock.instant(), traceId, correlationId);

            return new OrderflowEvent<>(metadata, payload);
        }

    }
}
