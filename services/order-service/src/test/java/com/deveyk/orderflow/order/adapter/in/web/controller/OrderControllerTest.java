package com.deveyk.orderflow.order.adapter.in.web.controller;

import com.deveyk.orderflow.order.adapter.in.web.facade.OrderFacade;
import com.deveyk.orderflow.order.adapter.in.web.mapper.OrderToResponseMapper;
import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.exception.ProductNotAvailableException;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderRequestBuilder;
import com.deveyk.orderflow.shared.observability.OrderflowHeaders;
import com.deveyk.orderflow.shared.testsupport.OrderflowRestControllerTest;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvcRequestBuilders;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockResultMatchersBuilders;
import com.deveyk.orderflow.shared.web.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;


@WebMvcTest(OrderController.class)
class OrderControllerTest extends OrderflowRestControllerTest {

    private static final String BASE_PATH = "/api/v1/orders";
    private static final UUID USER_ID = OrderValidTestData.Customer.ID;

    @MockitoBean
    private OrderFacade orderFacade;

    @Test
    @DisplayName("Geçerli sipariş isteği 202 döner; Location yeni siparişi, gövde orderId ve PENDING'i gösterir")
    void givenValidPlaceOrderRequest_whenPlaceOrder_thenReturnAcceptedWithLocation() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();
        final UUID orderId = OrderId.newId().value();
        Mockito.when(orderFacade.placeOrder(USER_ID, request))
                .thenReturn(PlaceOrderResponse.builder()
                        .orderId(orderId)
                        .status("PENDING")
                        .build());

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isAccepted())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, "http://localhost" + BASE_PATH + "/" + orderId))
                .andExpect(OrderflowMockResultMatchersBuilders.response("orderId").value(orderId.toString()))
                .andExpect(OrderflowMockResultMatchersBuilders.response("status").value("PENDING"));

        // Verify
        Mockito.verify(orderFacade, Mockito.times(1)).placeOrder(USER_ID, request);
    }

    @Test
    @DisplayName("X-User-Id header'ı olmayan istek 400 GEN_009 ile reddedilir")
    void givenNoUserHeader_whenPlaceOrder_thenReturnMissingHeaderError() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withValidValues()
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, request), CommonErrorCode.MISSING_REQUEST_HEADER)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @Test
    @DisplayName("UUID olmayan X-User-Id 400 GEN_004 ile reddedilir")
    void givenInvalidUserHeader_whenPlaceOrder_thenReturnTypeMismatchError() throws Exception {
        // Given
        final MockHttpServletRequestBuilder request = OrderflowMockMvcRequestBuilders
                .post(BASE_PATH, new PlaceOrderRequestBuilder().withValidValues().build())
                .header(OrderflowHeaders.USER_ID, "not-a-uuid");

        // When / Then
        orderflowMockMvc.perform(request, CommonErrorCode.TYPE_MISMATCH)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @Test
    @DisplayName("Satırı olmayan sipariş isteği 400 GEN_002 ile reddedilir; hatalı alan lines")
    void givenNoLines_whenPlaceOrder_thenReturnValidationError() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLines(List.of())
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request), CommonErrorCode.VALIDATION_ERROR)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest())
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorField(0).value("lines"))
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorMessage(0).value("At least one line is required"));

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @Test
    @DisplayName("Ürünü olmayan satır 400 GEN_002 ile reddedilir; hatalı alan productId")
    void givenLineWithoutProduct_whenPlaceOrder_thenReturnValidationError() throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLine(null, 1)
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request), CommonErrorCode.VALIDATION_ERROR)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest())
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorField(0).value("productId"))
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorMessage(0).value("Product id is required"));

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @ParameterizedTest(name = "quantity = {0}")
    @ValueSource(ints = {0, -1})
    @DisplayName("Adedi pozitif olmayan satır 400 GEN_002 ile reddedilir; hatalı alan quantity")
    void givenNonPositiveQuantity_whenPlaceOrder_thenReturnValidationError(final int quantity) throws Exception {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLine(OrderValidTestData.Product.ID, quantity)
                .build();

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request), CommonErrorCode.VALIDATION_ERROR)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest())
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorField(0).value("quantity"))
                .andExpect(OrderflowMockResultMatchersBuilders.subErrorMessage(0).value("Quantity must be at least 1"));

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @Test
    @DisplayName("Okunamayan gövde 400 GEN_003 ile reddedilir")
    void givenMalformedBody_whenPlaceOrder_thenReturnMalformedRequestError() throws Exception {
        // Given
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BASE_PATH)
                .header(OrderflowHeaders.USER_ID, USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"lines\": ");

        // When / Then
        orderflowMockMvc.perform(request, CommonErrorCode.MALFORMED_REQUEST)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }

    @Test
    @DisplayName("Kopyada olmayan ürün 422 ORD_023 döner")
    void givenUnavailableProduct_whenPlaceOrder_thenReturnBusinessRuleError() throws Exception {
        // Given
        final UUID unknown = UUID.randomUUID();
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLine(unknown, 1)
                .build();
        Mockito.when(orderFacade.placeOrder(USER_ID, request))
                .thenThrow(new ProductNotAvailableException(List.of(new ProductId(unknown))));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.post(BASE_PATH, USER_ID, request), OrderErrorCode.PRODUCT_NOT_AVAILABLE)
                .andExpect(OrderflowMockResultMatchersBuilders.status().is(422));

        // Verify
        Mockito.verify(orderFacade, Mockito.times(1)).placeOrder(USER_ID, request);
    }

    @Test
    @DisplayName("Sipariş okunur; tutarlar string, iptal sebebi PENDING siparişte yer almaz")
    void givenExistingOrder_whenGetOrder_thenReturnOrder() throws Exception {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();
        final OrderResponse response = OrderToResponseMapper.toOrderResponse(order);
        final UUID orderId = order.getId().value();
        Mockito.when(orderFacade.getOrder(USER_ID, orderId))
                .thenReturn(response);

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH + "/" + orderId, USER_ID))
                .andExpect(OrderflowMockResultMatchersBuilders.status().isOk())
                .andExpect(OrderflowMockResultMatchersBuilders.response("orderId").value(orderId.toString()))
                .andExpect(OrderflowMockResultMatchersBuilders.response("status").value("PENDING"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("cancellationReason").doesNotExist())
                .andExpect(OrderflowMockResultMatchersBuilders.response("placedAt").value("2026-11-27T10:00:00Z"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].productId")
                        .value(OrderValidTestData.Product.ID.toString()))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].productName")
                        .value(OrderValidTestData.Product.NAME))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].unitPrice.amount").value("249.90"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].unitPrice.currency").value("TRY"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("lines[0].quantity").value(1))
                .andExpect(OrderflowMockResultMatchersBuilders.response("total.amount").value("249.90"))
                .andExpect(OrderflowMockResultMatchersBuilders.response("version").doesNotExist());

        // Verify
        Mockito.verify(orderFacade, Mockito.times(1)).getOrder(USER_ID, orderId);
    }

    @Test
    @DisplayName("Bulunamayan sipariş 404 ORD_001 döner")
    void givenUnknownOrder_whenGetOrder_thenReturnNotFoundError() throws Exception {
        // Given
        final OrderId orderId = OrderId.newId();
        Mockito.when(orderFacade.getOrder(USER_ID, orderId.value()))
                .thenThrow(new OrderNotFoundException(orderId));

        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH + "/" + orderId.value(), USER_ID), OrderErrorCode.ORDER_NOT_FOUND)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isNotFound());

        // Verify
        Mockito.verify(orderFacade, Mockito.times(1)).getOrder(USER_ID, orderId.value());
    }

    @Test
    @DisplayName("UUID olmayan sipariş id'si 400 GEN_004 ile reddedilir")
    void givenInvalidOrderId_whenGetOrder_thenReturnTypeMismatchError() throws Exception {
        // When / Then
        orderflowMockMvc.perform(OrderflowMockMvcRequestBuilders.get(BASE_PATH + "/not-a-uuid", USER_ID), CommonErrorCode.TYPE_MISMATCH)
                .andExpect(OrderflowMockResultMatchersBuilders.status().isBadRequest());

        // Verify
        Mockito.verifyNoInteractions(orderFacade);
    }
}
