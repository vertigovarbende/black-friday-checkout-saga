package com.deveyk.orderflow.shared.messaging.contract.stock;

import lombok.Builder;

import java.util.UUID;

// stock.reserve.failed (event-catalog.md §2.2, §3). reason: UNKNOWN_PRODUCT, PRICE_CHANGED, INSUFFICIENT_STOCK,
// RESERVATION_CLOSED. productId sebebi olan satırın ürünü; RESERVATION_CLOSED'da null.
@Builder
public record StockReserveFailedPayload(UUID orderId, String reason, UUID productId) {

}
