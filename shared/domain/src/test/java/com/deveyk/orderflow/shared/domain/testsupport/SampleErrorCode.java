package com.deveyk.orderflow.shared.domain.testsupport;

import com.deveyk.orderflow.shared.domain.exception.ErrorCode;


public enum SampleErrorCode implements ErrorCode {

    SAMPLE_INVALID(
            "TST_001",
            "SAMPLE_INVALID",
            "Sample {0} can''t be {1}"
    ),

    SAMPLE_UNAVAILABLE(
            "TST_002",
            "SAMPLE_UNAVAILABLE",
            "Sample service is unavailable"
    );

    private final String code;
    private final String header;
    private final String defaultMessage;

    SampleErrorCode(final String code, final String header, final String defaultMessage) {
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
