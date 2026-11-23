package com.deveyk.orderflow.order.adapter.in.web.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PlaceOrderResponse(UUID orderId, String status) {
}
