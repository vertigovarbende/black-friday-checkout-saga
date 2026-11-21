package com.deveyk.orderflow.order.adapter.in.web.controller;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.application.order.port.OrderReadPort;
import com.deveyk.orderflow.order.application.order.port.OrderSavePort;
import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderRequestBuilder;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.observability.OrderflowHeaders;
import com.deveyk.orderflow.shared.testsupport.messaging.OutboxAssert;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvcRequestBuilders;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockResultMatchersBuilders;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.matchesPattern;

class OrderControllerIT extends OrderTestContainerConfiguration {

    private static final String BASE_PATH = "/api/v1/orders";
    private static final UUID USER_ID = OrderValidTestData.Customer.ID;
    private static final UUID SOMEONE_ELSE = UUID.fromString("0198a1b2-0000-7000-8000-000000000002");

    @Autowired
    private OrderReadPort orderReadPort;

    @Autowired
    private OrderSavePort orderSavePort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Sipariş verilir: 202 döner; kaydedilen siparişin satırlarında ad ve fiyat ürün kopyasındandır")
    void givenSeededProducts_whenPlaceOrder_thenAcceptedAndPendingOrderPersisted() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLines(List.of(
                        new PlaceOrderRequest.Line(OrderValidTestData.Product.ID, 2),
                        new PlaceOrderRequest.Line(OrderValidTestData.SecondProduct.ID, 1)))
                .build();

        // When
        final MvcResult result = orderflowMockMvc
                .perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                .andExpect(OrderflowMockResultMatchersBuilders.response("status").value("PENDING"))
                .andReturn();

        // Then
        final UUID orderId = UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.orderId"));
        assertThat(result.getResponse().getHeader("Location")).endsWith(BASE_PATH + "/" + orderId);

        // Verify
        final Order persisted = orderReadPort.findById(new OrderId(orderId)).orElseThrow();
        assertThat(persisted.getCustomerId()).isEqualTo(new CustomerId(USER_ID));
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(persisted.getLines())
                .extracting(OrderLine::getProductId, OrderLine::getProductName, OrderLine::getUnitPrice, OrderLine::getQuantity)
                .containsExactly(
                        tuple(new ProductId(OrderValidTestData.Product.ID), OrderValidTestData.Product.NAME, new Money(OrderValidTestData.Product.UNIT_PRICE, TRY), new Quantity(2)),
                        tuple(new ProductId(OrderValidTestData.SecondProduct.ID), OrderValidTestData.SecondProduct.NAME, new Money(OrderValidTestData.SecondProduct.UNIT_PRICE, TRY), new Quantity(1))
                );
        final OutboxAssert.OutboxRow event = OutboxAssert.of(jdbcTemplate).hasEvent(orderId.toString(), EventTypeNames.ORDER_CREATED);
        assertThat(event.topic()).isEqualTo(TopicNames.ORDER_CREATED);
    }

    @Test
    @DisplayName("Kopyada olmayan ürünle sipariş 422 ORD_023 döner ve hiçbir sipariş kaydedilmez")
    void givenUnknownProduct_whenPlaceOrder_thenBusinessRuleErrorAndNothingPersisted() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLines(List.of(
                        new PlaceOrderRequest.Line(OrderValidTestData.Product.ID, 1),
                        new PlaceOrderRequest.Line(UUID.randomUUID(), 1)))
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request), OrderErrorCode.PRODUCT_NOT_AVAILABLE)
                .andExpect(OrderflowMockResultMatchersBuilders.status().is(422));

        // Verify
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM orders", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM outbox_event", Integer.class)).isZero();
    }

    @Test
    @DisplayName("Müşteri kendi siparişini okur: 200, tutarlar string")
    void givenOwnOrder_whenGetOrder_thenReturnOrder() throws Exception {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withCustomerId(new CustomerId(USER_ID))
                .build();
        orderSavePort.create(order);

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH + "/" + order.getId().value(), USER_ID))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(OrderflowMockResultMatchersBuilders.response("orderId").value(order.getId().value().toString()))
                .andExpect(OrderflowMockResultMatchersBuilders.response("status").value("PENDING"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].productName")
                        .value(OrderValidTestData.Product.NAME))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].unitPrice.amount").value("249.90"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("total.amount").value("249.90"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("total.currency").value("TRY"));
    }

    @Test
    @DisplayName("Başka müşterinin siparişi yokmuş gibi davranılır: 404 ORD_001")
    void givenSomeoneElsesOrder_whenGetOrder_thenNotFound() throws Exception {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withCustomerId(new CustomerId(SOMEONE_ELSE))
                .build();
        orderSavePort.create(order);

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH + "/" + order.getId().value(), USER_ID), OrderErrorCode.ORDER_NOT_FOUND)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isNotFound())
                .andExpect(OrderflowMockResultMatchersBuilders.response("traceId").isNotEmpty());
    }

    @Test
    @DisplayName("Verilen X-Correlation-Id cevapta aynen döner; X-Trace-Id isteğin trace'ini gösterir")
    void givenCorrelationIdHeader_whenPlaceOrder_thenCorrelationIdEchoedAndTraceIdReturned() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request)
                        .header(OrderflowHeaders.CORRELATION_ID, "black-friday-checkout-1"))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                .andExpect(MockMvcResultMatchers.header().string(OrderflowHeaders.CORRELATION_ID, "black-friday-checkout-1"))
                .andExpect(MockMvcResultMatchers.header().string(OrderflowHeaders.TRACE_ID, matchesPattern("[0-9a-f]{32}")));
    }

    @Test
    @DisplayName("Verilen X-Correlation-Id ve isteğin trace'i order.created zarfına ve outbox satırına taşınır")
    void givenCorrelationIdHeader_whenPlaceOrder_thenOrderCreatedCarriesCorrelationAndTrace() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();

        // When
        final MvcResult result = orderflowMockMvc
                .perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request)
                        .header(OrderflowHeaders.CORRELATION_ID, "black-friday-checkout-1"))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                .andReturn();

        // Then
        final String orderId = JsonPath.read(result.getResponse().getContentAsString(), "$.orderId");
        final String traceId = result.getResponse().getHeader(OrderflowHeaders.TRACE_ID);
        final OutboxAssert.OutboxRow event = OutboxAssert.of(jdbcTemplate).hasEvent(orderId, EventTypeNames.ORDER_CREATED);
        assertThat(JsonPath.<String>read(event.payload(), "$.metadata.correlationId")).isEqualTo("black-friday-checkout-1");
        assertThat(JsonPath.<String>read(event.payload(), "$.metadata.traceId")).isEqualTo(traceId);
        assertThat(event.traceparent()).startsWith("00-" + traceId + "-");
    }

    @Test
    @DisplayName("X-Correlation-Id verilmezse correlationId isteğin traceId'sidir")
    void givenNoCorrelationIdHeader_whenPlaceOrder_thenCorrelationIdIsTraceId() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();

        // When
        final MvcResult result = orderflowMockMvc
                .perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                .andReturn();

        // Then
        assertThat(result.getResponse().getHeader(OrderflowHeaders.TRACE_ID)).isNotBlank();
        assertThat(result.getResponse().getHeader(OrderflowHeaders.CORRELATION_ID))
                .isEqualTo(result.getResponse().getHeader(OrderflowHeaders.TRACE_ID));
    }
}
