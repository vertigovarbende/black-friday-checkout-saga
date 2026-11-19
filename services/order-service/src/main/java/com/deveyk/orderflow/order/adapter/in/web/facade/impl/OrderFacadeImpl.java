package com.deveyk.orderflow.order.adapter.in.web.facade.impl;

import com.deveyk.orderflow.order.adapter.in.web.facade.OrderFacade;
import com.deveyk.orderflow.order.adapter.in.web.mapper.OrderWebMapper;
import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrderFacadeImpl implements OrderFacade {

    private final UseCaseHandler<Order, PlaceOrderUseCase> placeOrderUseCaseHandler;
    private final UseCaseHandler<Order, GetOrderUseCase> getOrderUseCaseHandler;
    private final OrderWebMapper orderWebMapper;

    @Override
    public PlaceOrderResponse placeOrder(final UUID userId, final PlaceOrderRequest request) {
        final Order order = placeOrderUseCaseHandler.handle(orderWebMapper.toPlaceOrderUseCase(userId, request));
        return orderWebMapper.toPlaceOrderResponse(order);
    }

    @Override
    public OrderResponse getOrder(final UUID userId, final UUID orderId) {
        final Order order = getOrderUseCaseHandler.handle(orderWebMapper.toGetOrderUseCase(userId, orderId));
        return orderWebMapper.toOrderResponse(order);
    }
}
