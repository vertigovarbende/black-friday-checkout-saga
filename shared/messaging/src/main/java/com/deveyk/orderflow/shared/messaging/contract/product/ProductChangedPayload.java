package com.deveyk.orderflow.shared.messaging.contract.product;

import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import lombok.Builder;

import java.util.UUID;

// productVersion: ürünün product-service'teki sürümü. Tüketici eski sürümü (yeniden yayın, geç gelen kopya) atlar.
@Builder
public record ProductChangedPayload(UUID productId,
                                    String name,
                                    MoneyAmount unitPrice,
                                    Long productVersion) {

}
