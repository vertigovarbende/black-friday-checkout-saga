package com.deveyk.orderflow.shared.inbox;

import com.deveyk.orderflow.shared.observability.MdcKeys;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class InboxProcessor {

    static final String DUPLICATE_COUNTER = "orderflow.inbox.duplicates";

    private final InboxEventPort inboxEventPort;
    private final MeterRegistry meterRegistry;

    public InboxProcessor(final InboxEventPort inboxEventPort, final MeterRegistry meterRegistry) {

        this.inboxEventPort = inboxEventPort;
        this.meterRegistry = meterRegistry;

    }

    @Transactional
    public void process(final InboxMessage message, final Runnable businessLogic) {

        final Map<String, String> context = mdcContext(message);
        context.forEach(MDC::put);

        try {
            if (!inboxEventPort.tryRecord(message)) {
                log.info("Duplicate message skipped consumer={} eventId={}", message.consumer(), message.eventId());
                meterRegistry.counter(DUPLICATE_COUNTER, "consumer", message.consumer()).increment();
                return;
            }

            log.debug("Message received");
            businessLogic.run();
            log.debug("Message processed");

        } finally {
            context.keySet().forEach(MDC::remove);
        }
    }

    private static Map<String, String> mdcContext(final InboxMessage message) {

        final Map<String, String> context = new LinkedHashMap<>();
        context.put(MdcKeys.CONSUMER, message.consumer());
        context.put(MdcKeys.EVENT_ID, message.eventId().toString());
        context.put(MdcKeys.EVENT_TYPE, message.eventType());
        context.put(MdcKeys.AGGREGATE_ID, message.aggregateId());

        // Zarfta yoksa uydurulmaz (CorrelationIdFilter'daki gibi).
        if (message.correlationId() != null) {
            context.put(MdcKeys.CORRELATION_ID, message.correlationId());
        }

        return context;
    }
}
