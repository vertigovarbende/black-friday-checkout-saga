package com.deveyk.orderflow.order.domain.common;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CommonDomainErrorCode implements ErrorCode {

    AMOUNT_REQUIRED(
            "ORD_101",
            "AMOUNT_REQUIRED",
            "Amount is required"
    ),
    CURRENCY_REQUIRED(
            "ORD_102",
            "CURRENCY_REQUIRED",
            "Currency is required"
    ),
    AMOUNT_NEGATIVE(
            "ORD_103",
            "AMOUNT_NEGATIVE",
            "Amount cannot be negative: {0}"
    ),
    AMOUNT_SCALE_TOO_LARGE(
            "ORD_104",
            "AMOUNT_SCALE_TOO_LARGE",
            "{0} allows at most {1} decimal places: {2}"
    ),
    CURRENCY_MISMATCH(
            "ORD_105",
            "CURRENCY_MISMATCH",
            "Cannot combine amounts in {0} and {1}"
    ),
    QUANTITY_NEGATIVE(
            "ORD_106",
            "QUANTITY_NEGATIVE",
            "Quantity cannot be negative: {0}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
