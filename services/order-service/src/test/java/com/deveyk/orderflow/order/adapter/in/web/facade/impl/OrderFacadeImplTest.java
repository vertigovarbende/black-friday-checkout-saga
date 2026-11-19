package com.deveyk.orderflow.order.adapter.in.web.facade.impl;

import com.deveyk.orderflow.order.adapter.in.web.mapper.OrderWebMapper;
import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderRequestBuilder;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderFacadeImplTest extends OrderflowUnitTest {

    private static final UUID USER_ID = OrderValidTestData.Customer.ID;

    @Mock
    private UseCaseHandler<Order, PlaceOrderUseCase> placeOrderUseCaseHandler;

    @Mock
    private UseCaseHandler<Order, GetOrderUseCase> getOrderUseCaseHandler;

    private OrderFacadeImpl facade;

    @BeforeEach
    void setUpFacade() {
        facade = new OrderFacadeImpl(placeOrderUseCaseHandler, getOrderUseCaseHandler, Mappers.getMapper(OrderWebMapper.class));
    }

    @Test
    @DisplayName("Sipariş isteği use case'e çevrilip handler'a bir kez verilir; dönen sipariş cevaba çevrilir")
    void givenPlaceOrderRequest_whenPlaceOrder_thenHandlerCalledAndResponseMapped() {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLine(OrderValidTestData.Product.ID, 2)
                .build();
        final PlaceOrderUseCase expectedUseCase = new PlaceOrderUseCase(new CustomerId(USER_ID), List.of(
                new PlaceOrderUseCase.Line(new ProductId(OrderValidTestData.Product.ID), new Quantity(2))));
        final Order placed = new OrderBuilder()
                .withValidValues()
                .build();
        Mockito.when(placeOrderUseCaseHandler.handle(expectedUseCase))
                .thenReturn(placed);

        // When
        final PlaceOrderResponse response = facade.placeOrder(USER_ID, request);

        // Then
        assertThat(response).isEqualTo(new PlaceOrderResponse(placed.getId().value(), "PENDING"));

        // Verify
        Mockito.verify(placeOrderUseCaseHandler, Mockito.times(1)).handle(expectedUseCase);
        Mockito.verifyNoInteractions(getOrderUseCaseHandler);
    }

    @Test
    @DisplayName("Sipariş okuma isteği handler'a bir kez verilir; dönen sipariş cevaba çevrilir")
    void givenOrderId_whenGetOrder_thenHandlerCalledAndResponseMapped() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();
        final GetOrderUseCase expectedUseCase = new GetOrderUseCase(order.getId(), new CustomerId(USER_ID));
        Mockito.when(getOrderUseCaseHandler.handle(expectedUseCase))
                .thenReturn(order);

        // When
        final OrderResponse response = facade.getOrder(USER_ID, order.getId().value());

        // Then
        assertThat(response.orderId()).isEqualTo(order.getId().value());
        assertThat(response.lines()).hasSize(1);

        // Verify
        Mockito.verify(getOrderUseCaseHandler, Mockito.times(1)).handle(expectedUseCase);
        Mockito.verifyNoInteractions(placeOrderUseCaseHandler);
    }

    @Test
    @DisplayName("Facade exception yakalamaz; handler'ın exception'ı olduğu gibi geçer (04 §1)")
    void givenHandlerThrows_whenGetOrder_thenExceptionPropagates() {
        // Given
        final OrderId orderId = OrderId.newId();
        final GetOrderUseCase useCase = new GetOrderUseCase(orderId, new CustomerId(USER_ID));
        Mockito.when(getOrderUseCaseHandler.handle(useCase))
                .thenThrow(new OrderNotFoundException(orderId));

        // When / Then
        assertThatThrownBy(() -> facade.getOrder(USER_ID, orderId.value()))
                .isInstanceOf(OrderNotFoundException.class);

        // Verify
        Mockito.verify(getOrderUseCaseHandler, Mockito.times(1)).handle(useCase);
    }
}
