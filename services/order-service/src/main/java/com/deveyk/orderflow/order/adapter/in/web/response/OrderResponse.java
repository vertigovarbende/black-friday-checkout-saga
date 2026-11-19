package com.deveyk.orderflow.order.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String status,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String cancellationReason,
        Instant placedAt,
        Instant lastModifiedAt,
        List<OrderLineResponse> lines,
        MoneyResponse total) {
}
