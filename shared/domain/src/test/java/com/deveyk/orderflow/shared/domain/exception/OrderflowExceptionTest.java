package com.deveyk.orderflow.shared.domain.exception;

import com.deveyk.orderflow.shared.domain.testsupport.SampleErrorCode;
import com.deveyk.orderflow.shared.domain.testsupport.SampleTechnicalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderflowExceptionTest {

    @Test
    @DisplayName("Mesaj, error code'un şablonundan argümanlarla kurulur ve kod exception'da kalır")
    void givenErrorCodeWithArgs_whenCreate_thenMessageFormattedAndCodeKept() {
        // When
        final DomainValidationException exception = new DomainValidationException(SampleErrorCode.SAMPLE_INVALID, "0193-abc", "PENDING");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Sample 0193-abc can't be PENDING");
        assertThat(exception.getErrorCode()).isEqualTo(SampleErrorCode.SAMPLE_INVALID);
    }

    @Test
    @DisplayName("Sebep (cause) verilen exception sebebi taşır")
    void givenCause_whenCreate_thenCauseKept() {
        // Given
        final IllegalStateException cause = new IllegalStateException("broker down");

        // When
        final SampleTechnicalException exception = new SampleTechnicalException(cause);

        // Then
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getMessage()).isEqualTo("Sample service is unavailable");
    }

    @Test
    @DisplayName("Somut exception kendi ailesine ve köke aittir")
    void givenConcreteException_whenCheckType_thenBelongsToFamilyAndRoot() {
        // When
        final DomainValidationException exception = new DomainValidationException(SampleErrorCode.SAMPLE_INVALID, "x", "y");

        // Then
        assertThat(exception)
                .isInstanceOf(OrderflowValidationException.class)
                .isInstanceOf(OrderflowException.class)
                .isInstanceOf(RuntimeException.class);
    }
}
