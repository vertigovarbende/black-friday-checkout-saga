package com.deveyk.orderflow.shared.messaging.contract.stock;

import lombok.Builder;

import java.util.UUID;

// Telafi C2 (orchestrator → product).
@Builder
public record StockReleaseRequestedPayload(UUID orderId) {

}
