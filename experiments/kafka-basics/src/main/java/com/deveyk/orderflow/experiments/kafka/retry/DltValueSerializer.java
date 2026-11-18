package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.common.serialization.Serializer;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

public class DltValueSerializer implements Serializer<Object> {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public byte[] serialize(final String topic, final Object data) {
        return switch (data) {
            case null -> null;
            case byte[] bytes -> bytes;
            case String text -> text.getBytes(StandardCharsets.UTF_8);
            default -> mapper.writeValueAsBytes(data);
        };
    }
}
