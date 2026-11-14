package com.deveyk.orderflow.shared.domain.exception;

public interface ErrorCode {

    String code();

    String header();

    String defaultMessage();
}
