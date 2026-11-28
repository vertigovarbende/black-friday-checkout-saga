package com.deveyk.orderflow.shared.messaging.contract.stock;

import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

// Saga T2 (orchestrator → product). unitPrice siparişin ürün kopyasındaki fiyattır; product-service güncel fiyatla
// karşılaştırır (PRICE_CHANGED).
@Builder
public record StockReserveRequestedPayload(UUID orderId,
                                           List<Line> lines) {

    @Builder
    public record Line(UUID productId, int quantity, MoneyAmount unitPrice) {

    }

}
