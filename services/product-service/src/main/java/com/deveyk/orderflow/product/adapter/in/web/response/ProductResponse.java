package com.deveyk.orderflow.product.adapter.in.web.response;

import lombok.Builder;

import java.util.UUID;

// onHand ve reserved da gösterilir: lab'da stoğun durumu buradan izlenir (flash sale deneyi, Faz 5).
@Builder
public record ProductResponse(UUID productId,
                              String name,
                              MoneyResponse unitPrice,
                              int onHand,
                              int reserved,
                              int available,
                              long catalogVersion) {

}
