package com.deveyk.orderflow.experiments.kafka.retry;

public class ValidationFailure extends RuntimeException {

    public ValidationFailure(final String message) {
        super(message);
    }
}
