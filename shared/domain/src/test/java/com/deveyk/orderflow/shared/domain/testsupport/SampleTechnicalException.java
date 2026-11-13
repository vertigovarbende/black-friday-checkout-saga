package com.deveyk.orderflow.shared.domain.testsupport;

import com.deveyk.orderflow.shared.domain.exception.OrderflowTechnicalException;

import java.io.Serial;

public final class SampleTechnicalException extends OrderflowTechnicalException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SampleTechnicalException(final Throwable cause) {
        super(SampleErrorCode.SAMPLE_UNAVAILABLE, cause);
    }

}
