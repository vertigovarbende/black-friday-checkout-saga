package com.deveyk.orderflow.shared.web.error;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_SERVER_ERROR(
            "GEN_001",
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred."
    ),
    VALIDATION_ERROR(
            "GEN_002",
            "VALIDATION_ERROR",
            "Validation failed."
    ),
    MALFORMED_REQUEST(
            "GEN_003",
            "MALFORMED_REQUEST",
            "Request body is missing or malformed."
    ),
    TYPE_MISMATCH(
            "GEN_004",
            "TYPE_MISMATCH",
            "A request parameter has an invalid type."
    ),
    RESOURCE_NOT_FOUND(
            "GEN_005",
            "RESOURCE_NOT_FOUND",
            "No resource found at this path."
    ),
    METHOD_NOT_ALLOWED(
            "GEN_006",
            "METHOD_NOT_ALLOWED",
            "HTTP method {0} is not supported for this path."
    ),
    DATA_CONFLICT(
            "GEN_007",
            "DATA_CONFLICT",
            "The request conflicts with existing data."
    ),
    UNSUPPORTED_MEDIA_TYPE(
            "GEN_008",
            "UNSUPPORTED_MEDIA_TYPE",
            "Content type {0} is not supported."
    ),
    MISSING_REQUEST_HEADER(
            "GEN_009",
            "MISSING_REQUEST_HEADER",
            "Required request header {0} is missing."
    ),
    CONCURRENT_MODIFICATION(
            "GEN_010",
            "CONCURRENT_MODIFICATION",
            "The resource was modified by another request. Please retry."
    ),
    SERVICE_UNAVAILABLE(
            "GEN_011",
            "SERVICE_UNAVAILABLE",
            "The service is temporarily unavailable. Please retry later."
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
