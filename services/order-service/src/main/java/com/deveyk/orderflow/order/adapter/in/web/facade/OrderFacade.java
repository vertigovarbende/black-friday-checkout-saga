package com.deveyk.orderflow.order.adapter.in.web.facade;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;

import java.util.UUID;

public interface OrderFacade {

    PlaceOrderResponse placeOrder(UUID userId, PlaceOrderRequest request);

    OrderResponse getOrder(UUID userId, UUID orderId);
}
