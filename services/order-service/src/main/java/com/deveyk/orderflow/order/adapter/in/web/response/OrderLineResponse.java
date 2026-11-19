package com.deveyk.orderflow.order.adapter.in.web.response;

import java.util.UUID;

public record OrderLineResponse(
        UUID productId,
        String productName,
        MoneyResponse unitPrice,
        int quantity,
        MoneyResponse lineTotal
) {

}
