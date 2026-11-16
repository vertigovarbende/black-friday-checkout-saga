package com.deveyk.orderflow.experiments.kafka.ordering;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CancelledListener {

    private static final Logger log = LoggerFactory.getLogger(CancelledListener.class);

    private final OrderingListener orderingListener;

    public CancelledListener(final OrderingListener orderingListener) {
        this.orderingListener = orderingListener;
    }

    @KafkaListener(topics = OrderingTopics.CANCELLED, groupId = "exp-ordering-cancelled")
    void onCancelled(final ConsumerRecord<String, String> record) {
        if (orderingListener.hasSeenCreated(record.key())) {
            log.info("cancelled after created key={}", record.key());
        } else {
            log.warn("CANCELLED BEFORE CREATED key={}", record.key());
        }
    }
}
