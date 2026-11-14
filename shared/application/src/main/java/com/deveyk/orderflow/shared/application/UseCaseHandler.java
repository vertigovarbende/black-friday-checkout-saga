package com.deveyk.orderflow.shared.application;

public interface UseCaseHandler<R, U extends UseCase> {

    R handle(U useCase);
}
