package com.deveyk.orderflow.product.adapter.in.web.response;

import lombok.Builder;

@Builder
public record MoneyResponse(String amount, String currency) {
}
