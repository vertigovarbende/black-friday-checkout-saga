package com.deveyk.orderflow.shared.messaging;

import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventJsonReaderTest {

    // shared-test-support'taki product.changed.v1 golden JSON'unun kopyası: shared modüller test-support'u kullanamaz
    // (01 §3.3 kural 5).
    private static final String ENVELOPE = """
            {
              "metadata": {
                "eventId": "0198a1b2-0000-7000-8000-00000000e101",
                "eventType": "ProductChanged",
                "aggregateType": "product",
                "aggregateId": "0198a1b2-0000-7000-8000-000000000101",
                "source": "product-service",
                "version": "1",
                "occurredAt": "2026-11-27T10:00:00Z",
                "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
                "correlationId": "4bf92f3577b34da6a3ce929d0e0e4736"
              },
              "payload": {
                "productId": "0198a1b2-0000-7000-8000-000000000101",
                "name": "Wireless Headphones Pro",
                "unitPrice": {
                  "amount": "279.90",
                  "currency": "TRY"
                },
                "productVersion": 3
              }
            }
            """;

    private final EventJsonReader reader = new EventJsonReader(JsonMapper.builder().build());

    @Test
    @DisplayName("Geçerli zarf metadata ve payload'ıyla okunur")
    void givenValidEnvelope_whenRead_thenMetadataAndPayloadRead() {
        // When
        final OrderflowEvent<ProductChangedPayload> event = reader.read(ENVELOPE, ProductChangedPayload.class);

        // Then
        assertThat(event.metadata().eventId()).isEqualTo(UUID.fromString("0198a1b2-0000-7000-8000-00000000e101"));
        assertThat(event.metadata().eventType()).isEqualTo("ProductChanged");
        assertThat(event.payload().name()).isEqualTo("Wireless Headphones Pro");
        assertThat(event.payload().productVersion()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Bilinmeyen alanlar yok sayılır: üreticinin eklediği yeni alan tüketiciyi bozmaz")
    void givenUnknownFields_whenRead_thenIgnored() {
        // Given
        final String json = ENVELOPE.replace("\"productVersion\": 3", "\"productVersion\": 3, \"category\": \"audio\"");

        // When
        final OrderflowEvent<ProductChangedPayload> event = reader.read(json, ProductChangedPayload.class);

        // Then
        assertThat(event.payload().productVersion()).isEqualTo(3L);
    }

    @ParameterizedTest(name = "[{index}] \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "{not json", "[]", "\"text\""})
    @DisplayName("Boş ya da JSON olarak okunamayan mesaj UNREADABLE_EVENT ile reddedilir")
    void givenUnreadableMessage_whenRead_thenUnreadableEvent(final String json) {
        // When / Then
        assertThatThrownBy(() -> reader.read(json, ProductChangedPayload.class))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.UNREADABLE_EVENT));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingFields")
    @DisplayName("Inbox'ın ihtiyaç duyduğu kimlik alanları ya da payload eksikse EVENT_FIELD_MISSING ile reddedilir")
    void givenMissingIdentityOrPayload_whenRead_thenEventFieldMissing(final String field, final String json) {
        // When / Then
        assertThatThrownBy(() -> reader.read(json, ProductChangedPayload.class))
                .isInstanceOfSatisfying(InvalidEventException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING);
                    assertThat(e.getMessage()).contains(field);
                });
    }

    private static Stream<Arguments> missingFields() {
        return Stream.of(
                Arguments.of("metadata", "{\"payload\": {\"productVersion\": 1}}"),
                Arguments.of("metadata.eventId", ENVELOPE.replace("\"eventId\": \"0198a1b2-0000-7000-8000-00000000e101\",", "")),
                Arguments.of("metadata.eventType", ENVELOPE.replace("\"ProductChanged\"", "\" \"")),
                Arguments.of("metadata.aggregateId", ENVELOPE.replace("\"aggregateId\": \"0198a1b2-0000-7000-8000-000000000101\",", "")),
                Arguments.of("payload", """
                        {"metadata": {"eventId": "0198a1b2-0000-7000-8000-00000000e101",
                                      "eventType": "ProductChanged",
                                      "aggregateId": "0198a1b2-0000-7000-8000-000000000101"}}
                        """));
    }
}
