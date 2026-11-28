package com.deveyk.orderflow.shared.observability;

public final class MdcKeys {

    public static final String CORRELATION_ID = "correlationId";

    // Kafka ile tetiklenen işler
    public static final String EVENT_ID = "eventId";
    public static final String EVENT_TYPE = "eventType";
    public static final String AGGREGATE_ID = "aggregateId";
    public static final String CONSUMER = "consumer";

    private MdcKeys() {
    }
}
