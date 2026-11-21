package com.deveyk.orderflow.shared.outbox;

import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;

class OutboxEventAdapter implements OutboxEventPort {

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;
    private final TraceContextProvider traceContextProvider;
    private final Clock clock;

    OutboxEventAdapter(final OutboxEventRepository outboxEventRepository,
                       final JsonMapper jsonMapper,
                       final TraceContextProvider traceContextProvider,
                       final Clock clock) {

        this.outboxEventRepository = outboxEventRepository;
        this.jsonMapper = jsonMapper;
        this.traceContextProvider = traceContextProvider;
        this.clock = clock;
    }

    // MANDATORY: kendi transaction'ını açsaydı iş verisinden bağımsız commit olurdu; outbox'ın anlamı kalmazdı.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void append(final String topic, final OrderflowEvent<?> event) {

        final EventMetadata metadata = event.metadata();

        outboxEventRepository.save(OutboxEventEntity.builder()
                .id(metadata.eventId())
                .aggregateType(metadata.aggregateType())
                .aggregateId(metadata.aggregateId())
                .eventType(metadata.eventType())
                .topic(topic)
                .payload(jsonMapper.writeValueAsString(event))
                .traceparent(traceContextProvider.currentTraceparent().orElse(null))
                .createdAt(clock.instant())
                .build());
    }
}
