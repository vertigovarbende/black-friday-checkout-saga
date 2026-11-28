package com.deveyk.orderflow.shared.observability.testsupport;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;

public enum ProbeErrorCode implements ErrorCode {

    PROBE("TST_001", "PROBE", "Probe failure");

    private final String code;
    private final String header;
    private final String defaultMessage;

    ProbeErrorCode(final String code, final String header, final String defaultMessage) {
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
