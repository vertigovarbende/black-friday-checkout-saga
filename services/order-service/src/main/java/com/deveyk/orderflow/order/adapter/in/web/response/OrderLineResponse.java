package com.deveyk.orderflow.order.adapter.in.web.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record OrderLineResponse(UUID productId,
                                String productName,
                                MoneyResponse unitPrice,
                                int quantity,
                                MoneyResponse lineTotal) {


}
