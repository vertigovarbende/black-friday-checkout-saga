package com.deveyk.orderflow.product.domain.reservation.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum StockReservationErrorCode implements ErrorCode {

    RESERVATION_NOT_FOUND(
            "PRD_201",
            "RESERVATION_NOT_FOUND",
            "Stock reservation not found for order {0}"
    ),
    INVALID_RESERVATION_STATE(
            "PRD_202",
            "INVALID_RESERVATION_STATE",
            "Stock reservation of order {0} cannot move from {1} to {2}"
    ),
    RESERVATION_ID_REQUIRED(
            "PRD_203",
            "RESERVATION_ID_REQUIRED",
            "Order id of the stock reservation is required"
    ),
    RESERVATION_STATUS_REQUIRED(
            "PRD_204",
            "RESERVATION_STATUS_REQUIRED",
            "Stock reservation status is required"
    ),
    RESERVATION_LINES_REQUIRED(
            "PRD_205",
            "RESERVATION_LINES_REQUIRED",
            "A {0} stock reservation needs at least one line"
    ),
    DUPLICATE_PRODUCT_LINE(
            "PRD_206",
            "DUPLICATE_PRODUCT_LINE",
            "Product {0} appears in more than one line"
    ),
    LINE_PRODUCT_REQUIRED(
            "PRD_207",
            "LINE_PRODUCT_REQUIRED",
            "Product of the reservation line is required"
    ),
    LINE_QUANTITY_MUST_BE_POSITIVE(
            "PRD_208",
            "LINE_QUANTITY_MUST_BE_POSITIVE",
            "Quantity of the reservation line must be at least 1"
    ),
    LINE_UNIT_PRICE_REQUIRED(
            "PRD_209",
            "LINE_UNIT_PRICE_REQUIRED",
            "Unit price of the requested line is required"
    ),
    REJECTION_REASON_REQUIRED(
            "PRD_210",
            "REJECTION_REASON_REQUIRED",
            "A rejected stock reservation needs a rejection reason"
    ),
    REJECTION_REASON_NOT_ALLOWED(
            "PRD_211",
            "REJECTION_REASON_NOT_ALLOWED",
            "A {0} stock reservation cannot have a rejection reason"
    ),
    REJECTED_PRODUCT_MISMATCH(
            "PRD_212",
            "REJECTED_PRODUCT_MISMATCH",
            "Rejection {0} has a product only when it is not RESERVATION_CLOSED"
    ),
    TIMESTAMP_REQUIRED(
            "PRD_213",
            "TIMESTAMP_REQUIRED",
            "Timestamp is required"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
