package com.deveyk.orderflow.shared.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TopicNamesTest {

    @Test
    @DisplayName("DLT adı topic ve consumer group'tan oluşur: <topic>.<group>.DLT")
    void givenTopicAndGroup_whenDeadLetter_thenTopicGroupDlt() {
        // When / Then
        assertThat(TopicNames.deadLetter(TopicNames.PRODUCT_CHANGED, "order-service"))
                .isEqualTo("product.changed.order-service.DLT");
    }
}
