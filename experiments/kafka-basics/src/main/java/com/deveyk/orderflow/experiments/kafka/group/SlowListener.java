package com.deveyk.orderflow.experiments.kafka.group;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SlowListener {

    private static final Logger log = LoggerFactory.getLogger(SlowListener.class);

    private final AtomicBoolean fixed = new AtomicBoolean();
    private final String instance;

    public SlowListener(@Value("${server.port}") final String instance) {
        this.instance = instance;
    }

    @KafkaListener(topics = GroupTopics.SLOW, groupId = "exp-slow", properties = "max.poll.interval.ms=10000")
    void onMessage(final ConsumerRecord<String, String> record) throws InterruptedException {
        final long processMs = fixed.get() ? 0 : Long.parseLong(record.value());

        log.info("slow start instance={} offset={} processMs={}", instance, record.offset(), processMs);
        Thread.sleep(processMs);
        log.info("slow done instance={} offset={}", instance, record.offset());
    }

    void fix() {
        fixed.set(true);
    }
}
