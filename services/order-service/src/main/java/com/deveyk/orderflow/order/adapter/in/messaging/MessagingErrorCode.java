package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// Gelen Kafka mesajının kendisindeki hatalar (06 §8: bozuk mesaj → retry yok, DLT). ORD_3xx.
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum MessagingErrorCode implements ErrorCode {

    UNREADABLE_EVENT(
            "ORD_301",
            "UNREADABLE_EVENT",
            "Event could not be read: {0}"
    ),
    EVENT_FIELD_MISSING(
            "ORD_302",
            "EVENT_FIELD_MISSING",
            "Event field {0} is missing"
    ),
    INVALID_EVENT_FIELD(
            "ORD_303",
            "INVALID_EVENT_FIELD",
            "Event field {0} is invalid: {1}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
