package com.deveyk.orderflow.order.adapter.in.web.response;

import java.util.UUID;

public record PlaceOrderResponse(UUID orderId, String status) {
}
