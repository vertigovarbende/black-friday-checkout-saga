package com.deveyk.orderflow.shared.messaging.contract.stock;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

// stock.reserved (event-catalog.md §2.2). reservedAt rezervasyonun oluştuğu an; yeniden yayında da aynı kalır.
@Builder
public record StockReservedPayload(UUID orderId, Instant reservedAt) {

}
