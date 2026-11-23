package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;

import java.io.Serial;

// Validation ailesinden: Kafka error handler retry yapmadan DLT'ye gönderir.
public final class InvalidEventException extends OrderflowValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidEventException(final MessagingErrorCode errorCode, final Object... args) {
        super(errorCode, args);
    }

    public InvalidEventException(final MessagingErrorCode errorCode, final Throwable cause, final Object... args) {
        super(errorCode, cause, args);
    }
}
