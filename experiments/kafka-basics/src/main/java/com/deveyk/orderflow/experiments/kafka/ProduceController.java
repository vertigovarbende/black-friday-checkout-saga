package com.deveyk.orderflow.experiments.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ProduceController {

    private final Map<String, KafkaTemplate<String, String>> templatesByAcks;

    public ProduceController(final KafkaTemplate<String, String> kafkaTemplate) {
        this.templatesByAcks = Map.of(
                "all", kafkaTemplate,
                "1", new KafkaTemplate<>(kafkaTemplate.getProducerFactory(), Map.of(ProducerConfig.ACKS_CONFIG, "1")),
                "0", new KafkaTemplate<>(kafkaTemplate.getProducerFactory(), Map.of(ProducerConfig.ACKS_CONFIG, "0")));
    }

    @PostMapping("/produce")
    public Map<String, Object> produce(@RequestParam final String topic,
                                       @RequestParam(required = false) final String key,
                                       @RequestParam final String value,
                                       @RequestParam(defaultValue = "all") final String acks) throws Exception {

        final KafkaTemplate<String, String> template = templatesByAcks.get(acks);

        if (template == null) {
            throw new IllegalArgumentException("acks must be one of all, 1, 0");
        }

        final long start = System.currentTimeMillis();

        final RecordMetadata metadata = template.send(topic, key, value)
                .get(30, TimeUnit.SECONDS)
                .getRecordMetadata();

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("acks", acks);
        result.put("topic", metadata.topic());
        result.put("partition", metadata.partition());
        result.put("offset", metadata.offset());
        result.put("tookMs", System.currentTimeMillis() - start);

        return result;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<String>> failed(final Exception exception) {
        final List<String> chain = new ArrayList<>();

        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            chain.add(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(chain);
    }
}
