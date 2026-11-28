package com.deveyk.orderflow.shared.messaging;

public record EventDefinition(String topic, String eventType, String aggregateType) {

}
