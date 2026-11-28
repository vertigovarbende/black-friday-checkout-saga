package com.deveyk.orderflow.product.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MoneyRequest(

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency

) {

}
