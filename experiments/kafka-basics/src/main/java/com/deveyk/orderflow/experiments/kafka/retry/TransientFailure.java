package com.deveyk.orderflow.experiments.kafka.retry;

public class TransientFailure extends RuntimeException {

    public TransientFailure(final String message) {
        super(message);
    }
}
