package com.deveyk.orderflow.experiments.kafka.retry;

public record OrderMessage(String orderId, int quantity) {
}
