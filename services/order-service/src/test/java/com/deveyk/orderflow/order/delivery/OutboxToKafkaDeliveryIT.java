package com.deveyk.orderflow.order.delivery;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.application.order.port.OrderDomainEventPort;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.testsupport.OrderDeliveryTestConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderRequestBuilder;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.observability.OrderflowHeaders;
import com.deveyk.orderflow.shared.testsupport.messaging.MessageProbe;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvcRequestBuilders;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockResultMatchersBuilders;
import com.jayway.jsonpath.JsonPath;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Teslim garantisi senaryoları 3 ve 4 (07 §9.3): outbox satırı gerçek connector ile Kafka'ya ulaşıyor mu?
class OutboxToKafkaDeliveryIT extends OrderDeliveryTestConfiguration {

    private static final String BASE_PATH = "/api/v1/orders";
    private static final UUID USER_ID = OrderValidTestData.Customer.ID;

    @Autowired
    private OrderDomainEventPort orderDomainEventPort;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verilen siparişin event'i Debezium ile order.created'a ulaşır: key orderId, değer outbox'taki zarf; id, eventType ve traceparent header'da")
    void givenPlacedOrder_whenDebeziumRelaysOutbox_thenOrderCreatedOnTopicWithKeyAndHeaders() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();

        try (MessageProbe probe = messageProbe(TopicNames.ORDER_CREATED)) {

            // When
            final MvcResult result = orderflowMockMvc
                    .perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request))
                    .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                    .andReturn();
            final String orderId = JsonPath.read(result.getResponse().getContentAsString(), "$.orderId");
            final String traceId = result.getResponse().getHeader(OrderflowHeaders.TRACE_ID);

            // Then
            final ConsumerRecord<String, String> message = probe.awaitMessage(orderId);
            final OutboxAssert.OutboxRow row = OutboxAssert.of(jdbcTemplate).hasEvent(orderId, EventTypeNames.ORDER_CREATED);

            assertThat(message.key()).isEqualTo(orderId);
            assertThat(MessageProbe.header(message, "id")).isEqualTo(row.id().toString());
            assertThat(MessageProbe.header(message, "eventType")).isEqualTo(EventTypeNames.ORDER_CREATED);
            assertThat(MessageProbe.header(message, "traceparent"))
                    .isEqualTo(row.traceparent())
                    .startsWith("00-" + traceId + "-");
            assertThat(JsonPath.<String>read(message.value(), "$.metadata.eventId")).isEqualTo(row.id().toString());
            assertThat(JsonPath.<String>read(message.value(), "$.metadata.eventType")).isEqualTo(EventTypeNames.ORDER_CREATED);
            assertThat(JsonPath.<String>read(message.value(), "$.payload.orderId")).isEqualTo(orderId);
        }
    }

    // Siparişin henüz tek event'i var; aynı aggregate'in ardışık event'leri adapter üzerinden yazılır.
    @Test
    @DisplayName("Aynı aggregate'in ardışık event'leri aynı partition'a, outbox'a yazıldıkları sırayla düşer")
    void givenConsecutiveEventsOfSameAggregate_whenDebeziumRelaysOutbox_thenSamePartitionInWriteOrder() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();
        final String orderId = order.getId().value().toString();

        try (MessageProbe probe = messageProbe(TopicNames.ORDER_CREATED)) {

            // When
            for (int i = 0; i < 3; i++) {
                transactionTemplate.executeWithoutResult(status -> orderDomainEventPort.publishOrderCreated(order));
            }

            // Then
            final List<ConsumerRecord<String, String>> messages = probe.awaitMessages(orderId, 3);
            // eventId UUIDv7: id sırası yazma sırasıdır.
            final List<String> writtenEventIds = jdbcTemplate.queryForList(
                    "SELECT id::text FROM outbox_event WHERE aggregate_id = ? ORDER BY id", String.class, orderId);

            assertThat(messages)
                    .extracting(ConsumerRecord::partition)
                    .containsOnly(messages.getFirst().partition());
            assertThat(messages)
                    .extracting(message -> MessageProbe.header(message, "id"))
                    .containsExactlyElementsOf(writtenEventIds);
        }
    }
}
