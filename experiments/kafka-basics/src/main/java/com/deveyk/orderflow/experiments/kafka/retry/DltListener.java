package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class DltListener {

    private static final Logger log = LoggerFactory.getLogger(DltListener.class);

    @KafkaListener(topics = {RetryTopics.RETRY_DLT, RetryTopics.POISON_DLT}, groupId = "exp-dlt-viewer")
    void onDeadLetter(final ConsumerRecord<String, String> record) {
        log.warn("DLT topic={} key={} value={} original={}@{} exception={} cause={} message={}",
                record.topic(),
                record.key(),
                record.value(),
                text(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                offset(record),
                text(record, KafkaHeaders.DLT_EXCEPTION_FQCN),
                text(record, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN),
                text(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE));
    }

    private static String text(final ConsumerRecord<?, ?> record, final String name) {
        final Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    private static Long offset(final ConsumerRecord<?, ?> record) {
        final Header header = record.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_OFFSET);
        return header == null ? null : ByteBuffer.wrap(header.value()).getLong();
    }
}
