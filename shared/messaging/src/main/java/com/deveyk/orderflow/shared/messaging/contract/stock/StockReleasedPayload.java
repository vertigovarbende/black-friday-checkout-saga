package com.deveyk.orderflow.shared.messaging.contract.stock;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

// stock.released (event-catalog.md §2.2). releasedAt rezervasyonun bırakıldığı (ya da kapanış kaydının açıldığı) an.
@Builder
public record StockReleasedPayload(UUID orderId, Instant releasedAt) {

}
