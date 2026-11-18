package com.deveyk.orderflow.order.domain.order.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(
            "ORD_001",
            "ORDER_NOT_FOUND",
            "Order not found: {0}"
    ),
    INVALID_ORDER_STATE(
            "ORD_002",
            "INVALID_ORDER_STATE",
            "Order {0} cannot move from {1} to {2}"
    ),
    ORDER_ID_REQUIRED(
            "ORD_003",
            "ORDER_ID_REQUIRED",
            "Order id is required"
    ),
    CUSTOMER_REQUIRED(
            "ORD_004",
            "CUSTOMER_REQUIRED",
            "Customer is required"
    ),
    ORDER_LINES_REQUIRED(
            "ORD_005",
            "ORDER_LINES_REQUIRED",
            "An order must have at least one line"
    ),
    TOO_MANY_LINES(
            "ORD_006",
            "TOO_MANY_LINES",
            "An order can have at most {0} lines"
    ),
    MIXED_CURRENCY(
            "ORD_007",
            "MIXED_CURRENCY",
            "All lines of an order must use the same currency"
    ),
    DUPLICATE_PRODUCT_LINE(
            "ORD_008",
            "DUPLICATE_PRODUCT_LINE",
            "Product {0} appears in more than one line"
    ),
    PLACED_AT_REQUIRED(
            "ORD_009",
            "PLACED_AT_REQUIRED",
            "Placement time is required"
    ),
    ORDER_STATUS_REQUIRED(
            "ORD_010",
            "ORDER_STATUS_REQUIRED",
            "Order status is required"
    ),
    CANCELLATION_REASON_REQUIRED(
            "ORD_011",
            "CANCELLATION_REASON_REQUIRED",
            "A cancelled order must have a cancellation reason"
    ),
    CANCELLATION_REASON_NOT_ALLOWED(
            "ORD_012",
            "CANCELLATION_REASON_NOT_ALLOWED",
            "A {0} order cannot have a cancellation reason"
    ),
    LAST_MODIFIED_AT_REQUIRED(
            "ORD_013",
            "LAST_MODIFIED_AT_REQUIRED",
            "Last modification time is required"
    ),
    TIMESTAMP_REQUIRED(
            "ORD_014",
            "TIMESTAMP_REQUIRED",
            "Timestamp is required"
    ),
    ORDER_LINE_ID_REQUIRED(
            "ORD_015",
            "ORDER_LINE_ID_REQUIRED",
            "Order line id is required"
    ),
    PRODUCT_REQUIRED(
            "ORD_016",
            "PRODUCT_REQUIRED",
            "Product is required"
    ),
    PRODUCT_NAME_REQUIRED(
            "ORD_017",
            "PRODUCT_NAME_REQUIRED",
            "Product name is required"
    ),
    UNIT_PRICE_REQUIRED(
            "ORD_018",
            "UNIT_PRICE_REQUIRED",
            "Unit price is required"
    ),
    UNIT_PRICE_MUST_BE_POSITIVE(
            "ORD_019",
            "UNIT_PRICE_MUST_BE_POSITIVE",
            "Unit price must be greater than zero"
    ),
    QUANTITY_REQUIRED(
            "ORD_020",
            "QUANTITY_REQUIRED",
            "Quantity is required"
    ),
    QUANTITY_MUST_BE_POSITIVE(
            "ORD_021",
            "QUANTITY_MUST_BE_POSITIVE",
            "Quantity must be at least 1"
    ),
    PRODUCT_NAME_TOO_LONG(
            "ORD_022",
            "PRODUCT_NAME_TOO_LONG",
            "Product name can be at most {0} characters"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
