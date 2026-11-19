package com.deveyk.orderflow.shared.testsupport.web;

import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;

public final class OrderflowMockResultMatchersBuilders {

    private OrderflowMockResultMatchersBuilders() {
    }

    public static StatusResultMatchers status() {
        return MockMvcResultMatchers.status();
    }

    public static JsonPathResultMatchers response(final String field) {
        return MockMvcResultMatchers.jsonPath("$." + field);
    }

    public static JsonPathResultMatchers timestamp() {
        return MockMvcResultMatchers.jsonPath("$.timestamp");
    }

    public static JsonPathResultMatchers code() {
        return MockMvcResultMatchers.jsonPath("$.code");
    }

    public static JsonPathResultMatchers header() {
        return MockMvcResultMatchers.jsonPath("$.header");
    }

    public static JsonPathResultMatchers message() {
        return MockMvcResultMatchers.jsonPath("$.message");
    }

    public static JsonPathResultMatchers path() {
        return MockMvcResultMatchers.jsonPath("$.path");
    }

    public static JsonPathResultMatchers subErrors() {
        return MockMvcResultMatchers.jsonPath("$.subErrors");
    }

    public static JsonPathResultMatchers subErrorField(final int index) {
        return MockMvcResultMatchers.jsonPath("$.subErrors[" + index + "].field");
    }

    public static JsonPathResultMatchers subErrorMessage(final int index) {
        return MockMvcResultMatchers.jsonPath("$.subErrors[" + index + "].message");
    }
}
