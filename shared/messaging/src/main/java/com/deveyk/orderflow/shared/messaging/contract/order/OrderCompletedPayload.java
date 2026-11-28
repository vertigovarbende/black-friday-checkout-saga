package com.deveyk.orderflow.shared.messaging.contract.order;

import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

// Siparişin son durumu (order-service, saga T5'ten sonra). product-service stoğu kesinleştirir, notification bildirir.
@Builder
public record OrderCompletedPayload(UUID orderId,
                                    UUID customerId,
                                    MoneyAmount totalAmount,
                                    Instant completedAt) {

}
