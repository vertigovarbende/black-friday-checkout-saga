package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PoisonListener {

    private static final Logger log = LoggerFactory.getLogger(PoisonListener.class);

    @KafkaListener(topics = RetryTopics.POISON, groupId = RetryTopics.POISON_GROUP, containerFactory = "poisonContainerFactory")
    void onMessage(final ConsumerRecord<String, OrderMessage> record) {
        log.info("poison-topic processed offset={} order={}", record.offset(), record.value());
    }
}
