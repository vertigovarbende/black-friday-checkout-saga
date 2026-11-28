package com.deveyk.orderflow.product.domain.product.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(
            "PRD_001",
            "PRODUCT_NOT_FOUND",
            "Product not found: {0}"
    ),
    PRODUCT_ID_REQUIRED(
            "PRD_002",
            "PRODUCT_ID_REQUIRED",
            "Product id is required"
    ),
    PRODUCT_NAME_REQUIRED(
            "PRD_003",
            "PRODUCT_NAME_REQUIRED",
            "Product name is required"
    ),
    PRODUCT_NAME_TOO_LONG(
            "PRD_004",
            "PRODUCT_NAME_TOO_LONG",
            "Product name can be at most {0} characters"
    ),
    UNIT_PRICE_REQUIRED(
            "PRD_005",
            "UNIT_PRICE_REQUIRED",
            "Unit price is required"
    ),
    UNIT_PRICE_MUST_BE_POSITIVE(
            "PRD_006",
            "UNIT_PRICE_MUST_BE_POSITIVE",
            "Unit price must be greater than zero"
    ),
    STOCK_REQUIRED(
            "PRD_007",
            "STOCK_REQUIRED",
            "On hand and reserved stock are required"
    ),
    RESERVED_EXCEEDS_ON_HAND(
            "PRD_008",
            "RESERVED_EXCEEDS_ON_HAND",
            "Product {0} has {1} reserved but only {2} on hand"
    ),
    CATALOG_VERSION_INVALID(
            "PRD_009",
            "CATALOG_VERSION_INVALID",
            "Catalog version must be at least 1: {0}"
    ),
    INSUFFICIENT_STOCK(
            "PRD_010",
            "INSUFFICIENT_STOCK",
            "Product {0} has {1} available, {2} requested"
    ),
    RESERVED_STOCK_INSUFFICIENT(
            "PRD_011",
            "RESERVED_STOCK_INSUFFICIENT",
            "Product {0} has {1} reserved, cannot release or commit {2}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
