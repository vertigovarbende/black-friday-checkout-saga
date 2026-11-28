package com.deveyk.orderflow.order.adapter.out.messaging;

import com.deveyk.orderflow.order.application.order.port.OrderDomainEventPort;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.observability.MdcKeys;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.jayway.jsonpath.JsonPath;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDomainEventAdapterIT extends OrderTestContainerConfiguration {

    @Autowired
    private OrderDomainEventPort orderDomainEventPort;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Tracer tracer;

    @Test
    @DisplayName("order.created outbox'a yazılır: topic, key, tip, zarf; traceparent ve correlationId aktif akıştan gelir")
    void givenActiveSpanAndCorrelationId_whenPublishOrderCreated_thenOutboxRowCarriesEventAndTraceContext() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();
        final String orderId = order.getId().value().toString();
        final Span span = tracer.nextSpan().name("place-order").start();

        // When
        try (Tracer.SpanInScope ignored = tracer.withSpan(span);
             MDC.MDCCloseable correlation = MDC.putCloseable(MdcKeys.CORRELATION_ID, "black-friday-checkout-1")) {
            transactionTemplate.executeWithoutResult(status -> orderDomainEventPort.publishOrderCreated(order));
        } finally {
            span.end();
        }

        // Then
        final OutboxAssert.OutboxRow row = OutboxAssert.of(jdbcTemplate).hasEvent(orderId, EventDefinitions.ORDER_CREATED.eventType());
        assertThat(row.topic()).isEqualTo(TopicNames.ORDER_CREATED);
        assertThat(row.aggregateType()).isEqualTo(EventDefinitions.ORDER_CREATED.aggregateType());
        assertThat(row.traceparent())
                .startsWith("00-" + span.context().traceId() + "-" + span.context().spanId() + "-");
        assertThat(Integer.parseInt(row.traceparent().substring(row.traceparent().length() - 2), 16) & 0x01)
                .as("sampled flag")
                .isEqualTo(1);

        assertThat(JsonPath.<String>read(row.payload(), "$.metadata.eventId")).isEqualTo(row.id().toString());
        assertThat(JsonPath.<String>read(row.payload(), "$.metadata.traceId")).isEqualTo(span.context().traceId());
        assertThat(JsonPath.<String>read(row.payload(), "$.metadata.correlationId")).isEqualTo("black-friday-checkout-1");
        assertThat(JsonPath.<String>read(row.payload(), "$.payload.orderId")).isEqualTo(orderId);
        assertThat(JsonPath.<String>read(row.payload(), "$.payload.totalAmount.amount")).isEqualTo("249.90");
        assertThat(JsonPath.<Integer>read(row.payload(), "$.payload.lines[0].quantity")).isEqualTo(1);
    }
}
