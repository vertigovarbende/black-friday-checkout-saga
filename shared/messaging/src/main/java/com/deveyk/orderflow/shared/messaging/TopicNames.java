package com.deveyk.orderflow.shared.messaging;

public final class TopicNames {

    public static final String ORDER_CREATED = "order.created";
    public static final String PRODUCT_CHANGED = "product.changed";

    private TopicNames() {
    }

    public static String deadLetter(final String topic, final String groupId) {
        return topic + "." + groupId + ".DLT";
    }

}
