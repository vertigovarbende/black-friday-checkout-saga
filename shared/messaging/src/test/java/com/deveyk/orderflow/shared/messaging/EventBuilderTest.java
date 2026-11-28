package com.deveyk.orderflow.shared.messaging;

import com.deveyk.orderflow.shared.domain.UuidV7;
import com.deveyk.orderflow.shared.messaging.contract.EventMetadata;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.observability.MdcKeys;
import com.deveyk.orderflow.shared.observability.TraceContextProvider;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventBuilderTest {

    private static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String PAYLOAD = "payload";

    @AfterEach
    void clearMdc() {
        MDC.remove(MdcKeys.CORRELATION_ID);
    }

    @Test
    @DisplayName("Zarf tanımdan gelen tip ve aggregate tipiyle, verilen aggregate id, payload ve kaynakla; saatten gelen zaman ve yeni bir UUIDv7 eventId ile kurulur")
    void givenAllFields_whenBuild_thenEnvelopeFromFieldsAndClock() {
        // Given
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(Tracer.NOOP), CLOCK);

        // When
        final OrderflowEvent<String> event = completeDraft(eventBuilder).build();

        // Then
        final EventMetadata metadata = event.metadata();
        assertThat(event.payload()).isSameAs(PAYLOAD);
        assertThat(metadata.eventId()).isNotNull();
        assertThat(metadata.eventId().version()).isEqualTo(7);
        assertThat(metadata.eventType()).isEqualTo("OrderCreated");
        assertThat(metadata.aggregateType()).isEqualTo("order");
        assertThat(metadata.aggregateId()).isEqualTo("order-42");
        assertThat(metadata.source()).isEqualTo("order-service");
        assertThat(metadata.version()).isEqualTo(EventBuilder.SCHEMA_VERSION);
        assertThat(metadata.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("MDC'de correlationId varsa o kullanılır; traceId aktif span'den gelir")
    void givenCorrelationIdInMdc_whenBuild_thenCorrelationIdFromMdc() {
        // Given
        MDC.put(MdcKeys.CORRELATION_ID, "black-friday-checkout-1");
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(tracerWithTraceId()), CLOCK);

        // When
        final EventMetadata metadata = completeDraft(eventBuilder).build().metadata();

        // Then
        assertThat(metadata.traceId()).isEqualTo(TRACE_ID);
        assertThat(metadata.correlationId()).isEqualTo("black-friday-checkout-1");
    }

    @Test
    @DisplayName("MDC'de correlationId yoksa akışın başı burasıdır: correlationId traceId olur")
    void givenNoCorrelationIdInMdc_whenBuild_thenCorrelationIdIsTraceId() {
        // Given
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(tracerWithTraceId()), CLOCK);

        // When
        final EventMetadata metadata = completeDraft(eventBuilder).build().metadata();

        // Then
        assertThat(metadata.correlationId()).isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("Tracing kapalı ve MDC boşsa traceId ve correlationId null'dır; uydurulmaz")
    void givenNoTracingAndEmptyMdc_whenBuild_thenTraceFieldsNull() {
        // Given
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(Tracer.NOOP), CLOCK);

        // When
        final EventMetadata metadata = completeDraft(eventBuilder).build().metadata();

        // Then
        assertThat(metadata.traceId()).isNull();
        assertThat(metadata.correlationId()).isNull();
    }

    @Test
    @DisplayName("Ardışık event'lerin eventId'leri farklıdır ve zamana göre sıralıdır")
    void givenTwoEvents_whenBuild_thenEventIdsDifferAndAreTimeOrdered() {
        // Given
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(Tracer.NOOP), CLOCK);

        // When
        final EventMetadata first = completeDraft(eventBuilder).build().metadata();
        final EventMetadata second = completeDraft(eventBuilder).build().metadata();

        // Then
        assertThat(first.eventId()).isNotEqualTo(second.eventId());
        assertThat(UuidV7.timestampOf(first.eventId())).isLessThanOrEqualTo(UuidV7.timestampOf(second.eventId()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incompleteDrafts")
    @DisplayName("Zorunlu bir alan eksik ya da boşsa build() durur; varsayılan değerle event üretilmez")
    void givenMissingRequiredField_whenBuild_thenIllegalState(final String missing, final EventBuilder.Draft<String> draft) {
        // When / Then
        assertThatThrownBy(draft::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(missing);
    }

    static Stream<Arguments> incompleteDrafts() {
        final EventBuilder eventBuilder = new EventBuilder(new TraceContextProvider(Tracer.NOOP), CLOCK);
        return Stream.of(
                Arguments.of("definition", eventBuilder.newEvent(null, "order-42", PAYLOAD).withSource("order-service")),
                Arguments.of("payload", eventBuilder.<String>newEvent(EventDefinitions.ORDER_CREATED, "order-42", null)
                        .withSource("order-service")),
                Arguments.of("aggregateId", eventBuilder.newEvent(EventDefinitions.ORDER_CREATED, null, PAYLOAD)
                        .withSource("order-service")),
                Arguments.of("aggregateId", eventBuilder.newEvent(EventDefinitions.ORDER_CREATED, " ", PAYLOAD)
                        .withSource("order-service")),
                Arguments.of("source", eventBuilder.newEvent(EventDefinitions.ORDER_CREATED, "order-42", PAYLOAD)),
                Arguments.of("source", completeDraft(eventBuilder).withSource(""))
        );
    }

    private static EventBuilder.Draft<String> completeDraft(final EventBuilder eventBuilder) {
        return eventBuilder.newEvent(EventDefinitions.ORDER_CREATED, "order-42", PAYLOAD)
                .withSource("order-service");
    }

    private static Tracer tracerWithTraceId() {
        final Tracer tracer = Mockito.mock(Tracer.class);
        final Span span = Mockito.mock(Span.class);
        final TraceContext context = Mockito.mock(TraceContext.class);
        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(context);
        Mockito.when(context.traceId()).thenReturn(TRACE_ID);
        return tracer;
    }
}
