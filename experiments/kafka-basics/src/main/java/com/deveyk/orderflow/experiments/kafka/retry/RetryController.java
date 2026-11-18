package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/retry")
public class RetryController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public RetryController(final KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestParam(defaultValue = RetryTopics.RETRY) final String topic,
                                    @RequestParam(defaultValue = "k") final String key,
                                    @RequestParam final String orderId,
                                    @RequestParam(defaultValue = "1") final int quantity) throws Exception {

        return send(topic, key, mapper.writeValueAsString(new OrderMessage(orderId, quantity)));
    }

    @PostMapping("/raw")
    public Map<String, Object> raw(@RequestParam(defaultValue = RetryTopics.POISON) final String topic,
                                   @RequestParam(defaultValue = "k") final String key,
                                   @RequestParam final String value) throws Exception {

        return send(topic, key, value);
    }

    private Map<String, Object> send(final String topic, final String key, final String value) throws Exception {
        final RecordMetadata metadata = kafkaTemplate.send(topic, key, value)
                .get(10, TimeUnit.SECONDS)
                .getRecordMetadata();

        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("topic", metadata.topic());
        result.put("partition", metadata.partition());
        result.put("offset", metadata.offset());
        result.put("value", value);

        return result;
    }
}
