package com.deveyk.orderflow.shared.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicPropertiesTest {

    @Test
    @DisplayName("Topic ayarlardaki partition, replication factor ve min ISR ile açılır")
    void givenProperties_whenNewTopic_thenTopicWithConfiguredSettings() {
        // Given
        final KafkaTopicProperties properties = new KafkaTopicProperties(3, 3, 2);

        // When
        final NewTopic topic = properties.newTopic("order.created");

        // Then
        assertThat(topic.name()).isEqualTo("order.created");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 3);
        assertThat(topic.configs()).containsEntry(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2");
    }
}
