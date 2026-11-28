package com.deveyk.orderflow.order.domain.product.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ProductSnapshotErrorCode implements ErrorCode {

    PRODUCT_ID_REQUIRED(
            "ORD_201",
            "PRODUCT_ID_REQUIRED",
            "Product id is required"
    ),
    PRODUCT_NAME_REQUIRED(
            "ORD_202",
            "PRODUCT_NAME_REQUIRED",
            "Product name is required"
    ),
    PRODUCT_NAME_TOO_LONG(
            "ORD_203",
            "PRODUCT_NAME_TOO_LONG",
            "Product name can be at most {0} characters"
    ),
    UNIT_PRICE_REQUIRED(
            "ORD_204",
            "UNIT_PRICE_REQUIRED",
            "Unit price is required"
    ),
    UNIT_PRICE_MUST_BE_POSITIVE(
            "ORD_205",
            "UNIT_PRICE_MUST_BE_POSITIVE",
            "Unit price must be greater than zero"
    ),
    PRODUCT_VERSION_NEGATIVE(
            "ORD_206",
            "PRODUCT_VERSION_NEGATIVE",
            "Product version cannot be negative: {0}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
