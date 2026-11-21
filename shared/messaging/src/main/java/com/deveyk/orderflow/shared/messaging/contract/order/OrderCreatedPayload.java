package com.deveyk.orderflow.shared.messaging.contract.order;

import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record OrderCreatedPayload(UUID orderId,
                                  UUID customerId,
                                  Instant placedAt,
                                  MoneyAmount totalAmount,
                                  List<Line> lines) {

    @Builder
    public record Line(UUID productId, int quantity, MoneyAmount unitPrice) {

    }

}
