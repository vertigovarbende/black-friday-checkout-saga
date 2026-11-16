package com.deveyk.orderflow.experiments.kafka.ordering;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderingListener {

    private static final Logger log = LoggerFactory.getLogger(OrderingListener.class);
    private static final long PROCESSING_MS = 100;

    private final Map<String, Integer> lastSequenceByKey = new ConcurrentHashMap<>();
    private final Set<String> createdKeys = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = OrderingTopics.CREATED, groupId = "exp-ordering-created")
    void onCreated(final ConsumerRecord<String, String> record) throws InterruptedException {
        Thread.sleep(PROCESSING_MS);

        log.info("created partition={} offset={} key={} value={}", record.partition(), record.offset(), record.key(), record.value());

        checkSequence(record.key(), record.value());
        createdKeys.add(record.key());
    }

    boolean hasSeenCreated(final String key) {
        return createdKeys.contains(key);
    }

    private void checkSequence(final String key, final String value) {
        final int separator = value.lastIndexOf('#');

        if (separator < 0) {
            return;
        }

        final int sequence = Integer.parseInt(value.substring(separator + 1));
        final Integer last = lastSequenceByKey.put(key, sequence);

        if (last != null && sequence != 1 && sequence != last + 1) {
            log.warn("OUT OF ORDER key={} expected={} got={}", key, last + 1, sequence);
        }
    }
}
