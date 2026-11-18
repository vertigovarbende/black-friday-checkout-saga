package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RetryListener {

    private static final Logger log = LoggerFactory.getLogger(RetryListener.class);
    private static final int FLAKY_SUCCEEDS_ON = 3;

    private final Map<String, Integer> attemptsByRecord = new ConcurrentHashMap<>();

    @KafkaListener(topics = RetryTopics.RETRY, groupId = RetryTopics.RETRY_GROUP, containerFactory = "retryContainerFactory")
    void onMessage(final ConsumerRecord<String, OrderMessage> record) {
        final OrderMessage order = record.value();
        final int attempt = attemptsByRecord.merge(record.partition() + "@" + record.offset(), 1, Integer::sum);

        log.info("attempt={} partition={} offset={} key={} order={}",
                attempt, record.partition(), record.offset(), record.key(), order);

        if (order.quantity() <= 0) {
            throw new ValidationFailure("Quantity must be positive: " + order.quantity());
        }

        if (order.orderId().startsWith("down")) {
            throw new TransientFailure("Downstream unavailable, attempt " + attempt);
        }

        if (order.orderId().startsWith("flaky") && attempt < FLAKY_SUCCEEDS_ON) {
            throw new TransientFailure("Flaky downstream, attempt " + attempt);
        }

        log.info("processed partition={} offset={} order={}", record.partition(), record.offset(), order);
    }
}
