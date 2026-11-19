package com.deveyk.orderflow.shared.observability.testsupport;

import com.deveyk.orderflow.shared.application.UseCase;

public record ProbeUseCase(RuntimeException failure) implements UseCase {

    public static ProbeUseCase succeeding() {
        return new ProbeUseCase(null);
    }

    public static ProbeUseCase failingWith(final RuntimeException failure) {
        return new ProbeUseCase(failure);
    }
}
