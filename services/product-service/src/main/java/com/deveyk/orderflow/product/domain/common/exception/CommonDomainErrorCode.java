package com.deveyk.orderflow.product.domain.common.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CommonDomainErrorCode implements ErrorCode {

    AMOUNT_REQUIRED(
            "PRD_101",
            "AMOUNT_REQUIRED",
            "Amount is required"
    ),
    CURRENCY_REQUIRED(
            "PRD_102",
            "CURRENCY_REQUIRED",
            "Currency is required"
    ),
    AMOUNT_NEGATIVE(
            "PRD_103",
            "AMOUNT_NEGATIVE",
            "Amount cannot be negative: {0}"
    ),
    AMOUNT_SCALE_TOO_LARGE(
            "PRD_104",
            "AMOUNT_SCALE_TOO_LARGE",
            "{0} allows at most {1} decimal places: {2}"
    ),
    QUANTITY_NEGATIVE(
            "PRD_105",
            "QUANTITY_NEGATIVE",
            "Quantity cannot be negative: {0}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
