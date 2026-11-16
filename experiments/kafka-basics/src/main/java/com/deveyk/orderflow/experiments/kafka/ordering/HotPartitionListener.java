package com.deveyk.orderflow.experiments.kafka.ordering;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class HotPartitionListener {

    private static final Logger log = LoggerFactory.getLogger(HotPartitionListener.class);
    private static final long PROCESSING_MS = 20;
    private static final int LOG_EVERY = 100;

    private final Map<Integer, AtomicInteger> processedByPartition = new ConcurrentHashMap<>();

    @KafkaListener(topics = OrderingTopics.HOT, groupId = "exp-hot", concurrency = "3")
    void onHot(final ConsumerRecord<String, String> record) throws InterruptedException {
        Thread.sleep(PROCESSING_MS);

        final int processed = processedByPartition
                .computeIfAbsent(record.partition(), partition -> new AtomicInteger())
                .incrementAndGet();

        if (processed == 1 || processed % LOG_EVERY == 0) {
            log.info("hot partition={} processed={} offset={}", record.partition(), processed, record.offset());
        }
    }
}
