package com.deveyk.orderflow.shared.kafka.testsupport;

import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;

import java.io.Serial;

// Not-found ailesi abstract; sınıflandırma testleri için somut bir örnek.
public final class TestResourceNotFoundException extends OrderflowResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TestResourceNotFoundException() {
        super(SharedErrorCode.ID_REQUIRED, "checkoutSaga");
    }
}
