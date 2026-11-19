package com.deveyk.orderflow.shared.observability.testsupport;

import com.deveyk.orderflow.shared.application.UseCaseHandler;

public class ProbeUseCaseHandler implements UseCaseHandler<String, ProbeUseCase> {

    @Override
    public String handle(final ProbeUseCase useCase) {
        if (useCase.failure() != null) {
            throw useCase.failure();
        }
        return "done";
    }
}
