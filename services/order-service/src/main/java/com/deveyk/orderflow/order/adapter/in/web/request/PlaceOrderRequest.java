package com.deveyk.orderflow.order.adapter.in.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record PlaceOrderRequest(
        @Valid
        @NotEmpty(message = "At least one line is required")
        List<@NotNull(message = "Line is required") Line> lines
) {

    public record Line(

            @NotNull(message = "Product id is required")
            UUID productId,

            @Positive(message = "Quantity must be at least 1")
            int quantity

    ) {

    }
}
