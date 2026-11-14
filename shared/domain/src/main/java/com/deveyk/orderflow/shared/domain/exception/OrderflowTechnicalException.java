package com.deveyk.orderflow.shared.domain.exception;

import java.io.Serial;

public abstract class OrderflowTechnicalException extends OrderflowException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderflowTechnicalException(final ErrorCode errorCode, final Object... args) {
        super(errorCode, args);
    }

    protected OrderflowTechnicalException(final ErrorCode errorCode, final Throwable cause, final Object... args) {
        super(errorCode, cause, args);
    }
}
