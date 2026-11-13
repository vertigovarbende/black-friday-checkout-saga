package com.deveyk.orderflow.shared.domain.exception;

import java.io.Serial;

public final class DomainValidationException extends OrderflowValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DomainValidationException(final ErrorCode errorCode, final Object... args) {
        super(errorCode, args);
    }
}
