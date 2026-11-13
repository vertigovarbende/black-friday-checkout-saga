package com.deveyk.orderflow.shared.domain.exception;

public enum SharedErrorCode implements ErrorCode {

    ID_REQUIRED(
            "SHR_001",
            "ID_REQUIRED",
            "{0} is required"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;

    SharedErrorCode(final String code, final String header, final String defaultMessage) {
        this.code = code;
        this.header = header;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String header() {
        return header;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
