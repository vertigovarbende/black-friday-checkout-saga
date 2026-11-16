package com.deveyk.orderflow.experiments.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LoggingListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingListener.class);

    @KafkaListener(topics = ExperimentTopics.ORDERS)
    void onOrder(final ConsumerRecord<String, String> record) {
        log.info("received topic={} partition={} offset={} key={} value={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
