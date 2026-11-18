package com.deveyk.orderflow.experiments.kafka.group;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GroupListener {

    private static final Logger log = LoggerFactory.getLogger(GroupListener.class);
    private static final long PROCESSING_MS = 200;

    private final ProcessedLedger ledger;
    private final String instance;

    public GroupListener(final ProcessedLedger ledger, @Value("${server.port}") final String instance) {
        this.ledger = ledger;
        this.instance = instance;
    }

    @KafkaListener(topics = GroupTopics.GROUP, groupId = "exp-group",
            properties = {"group.protocol=${experiments.group-protocol:classic}", "max.poll.records=10"})
    void onMessage(final ConsumerRecord<String, String> record) throws InterruptedException {
        Thread.sleep(PROCESSING_MS);

        ledger.append(record.value(), instance);
        log.info("processed instance={} partition={} offset={} value={}",
                instance, record.partition(), record.offset(), record.value());
    }
}
