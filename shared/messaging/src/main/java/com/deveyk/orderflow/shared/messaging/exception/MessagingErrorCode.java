package com.deveyk.orderflow.shared.messaging.exception;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

// Gelen mesajın kendisindeki hatalar (06 §8: bozuk mesaj → retry yok, DLT). MSG öneki; order-service'teki eski
// karşılıkları ORD_301–303 emekli (06 §3.3).
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum MessagingErrorCode implements ErrorCode {

    UNREADABLE_EVENT(
            "MSG_001",
            "UNREADABLE_EVENT",
            "Event could not be read: {0}"
    ),
    EVENT_FIELD_MISSING(
            "MSG_002",
            "EVENT_FIELD_MISSING",
            "Event field {0} is missing"
    ),
    INVALID_EVENT_FIELD(
            "MSG_003",
            "INVALID_EVENT_FIELD",
            "Event field {0} is invalid: {1}"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;
}
