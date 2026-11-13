package com.deveyk.orderflow.shared.domain.exception;

import java.io.Serial;
import java.text.MessageFormat;

public abstract class OrderflowException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final transient Object[] args;

    protected OrderflowException(final ErrorCode errorCode, final Object... args) {
        super(MessageFormat.format(errorCode.defaultMessage(), args));
        this.errorCode = errorCode;
        this.args = args;
    }

    protected OrderflowException(final ErrorCode errorCode, final Throwable cause, final Object... args) {
        super(MessageFormat.format(errorCode.defaultMessage(), args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
