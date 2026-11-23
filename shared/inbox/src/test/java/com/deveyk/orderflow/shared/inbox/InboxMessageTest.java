package com.deveyk.orderflow.shared.inbox;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboxMessageTest {

    private static final UUID EVENT_ID = UuidV7.next();

    @Test
    @DisplayName("Zarfın metadata'sından consumer ile birlikte kurulur")
    void givenEventMetadata_whenOf_thenFieldsFromMetadata() {
        // Given
        final EventMetadata metadata = new EventMetadata(EVENT_ID, "ProbeChanged", "probe", "probe-1", "probe-service",
                "1", Instant.parse("2026-11-27T10:00:00Z"), "4bf92f3577b34da6a3ce929d0e0e4736", "black-friday-checkout-1");

        // When
        final InboxMessage message = InboxMessage.of("order-service", metadata);

        // Then
        assertThat(message).isEqualTo(new InboxMessage("order-service", EVENT_ID, "ProbeChanged", "probe-1",
                "black-friday-checkout-1"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidMessages")
    @DisplayName("Consumer, eventId, eventType ya da aggregateId eksikse kurulamaz")
    void givenMissingField_whenCreate_thenIllegalArgument(final String field, final String consumer, final UUID eventId,
                                                          final String eventType, final String aggregateId) {
        // When / Then
        assertThatThrownBy(() -> new InboxMessage(consumer, eventId, eventType, aggregateId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inbox " + field + " is required");
    }

    private static Stream<Arguments> invalidMessages() {
        return Stream.of(
                Arguments.of("consumer", null, EVENT_ID, "ProbeChanged", "probe-1"),
                Arguments.of("consumer", " ", EVENT_ID, "ProbeChanged", "probe-1"),
                Arguments.of("eventId", "order-service", null, "ProbeChanged", "probe-1"),
                Arguments.of("eventType", "order-service", EVENT_ID, "", "probe-1"),
                Arguments.of("aggregateId", "order-service", EVENT_ID, "ProbeChanged", null));
    }
}
