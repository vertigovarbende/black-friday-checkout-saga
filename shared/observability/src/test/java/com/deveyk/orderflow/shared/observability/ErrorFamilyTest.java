package com.deveyk.orderflow.shared.observability;

import com.deveyk.orderflow.shared.domain.exception.OrderflowBusinessRuleException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowConflictException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowInvalidStateException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowResourceNotFoundException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowTechnicalException;
import com.deveyk.orderflow.shared.domain.exception.OrderflowValidationException;
import com.deveyk.orderflow.shared.observability.testsupport.ProbeErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorFamilyTest {

    static Stream<Arguments> families() {
        return Stream.of(
                Arguments.of(new OrderflowValidationException(ProbeErrorCode.PROBE) { }, ErrorFamily.VALIDATION),
                Arguments.of(new OrderflowResourceNotFoundException(ProbeErrorCode.PROBE) { }, ErrorFamily.NOT_FOUND),
                Arguments.of(new OrderflowConflictException(ProbeErrorCode.PROBE) { }, ErrorFamily.CONFLICT),
                Arguments.of(new OrderflowInvalidStateException(ProbeErrorCode.PROBE) { }, ErrorFamily.INVALID_STATE),
                Arguments.of(new OrderflowBusinessRuleException(ProbeErrorCode.PROBE) { }, ErrorFamily.BUSINESS_RULE),
                Arguments.of(new OrderflowTechnicalException(ProbeErrorCode.PROBE) { }, ErrorFamily.TECHNICAL)
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("families")
    @DisplayName("Her exception ailesi metrik etiketindeki kendi adına eşlenir")
    void givenFamilyException_whenOf_thenFamilyName(final Throwable exception, final String expected) {
        assertThat(ErrorFamily.of(exception)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Aileye ait olmayan exception unexpected olarak sayılır")
    void givenNonOrderflowException_whenOf_thenUnexpected() {
        assertThat(ErrorFamily.of(new IllegalStateException("boom"))).isEqualTo(ErrorFamily.UNEXPECTED);
    }
}
