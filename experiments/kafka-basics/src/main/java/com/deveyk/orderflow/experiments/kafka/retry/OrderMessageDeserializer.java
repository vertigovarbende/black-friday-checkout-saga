package com.deveyk.orderflow.experiments.kafka.retry;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

public class OrderMessageDeserializer implements Deserializer<OrderMessage> {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    public OrderMessage deserialize(final String topic, final byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.readValue(data, OrderMessage.class);
        } catch (final JacksonException e) {
            throw new SerializationException("Invalid order message on " + topic, e);
        }
    }
}
