package com.deveyk.orderflow.order.adapter.in.web.response;

import lombok.Builder;

@Builder
public record MoneyResponse(String amount, String currency) {
}
