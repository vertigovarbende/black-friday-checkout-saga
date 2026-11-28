package com.deveyk.orderflow.shared.kafka;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;
import com.deveyk.orderflow.shared.kafka.testsupport.TestResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ListenerExecutionFailedException;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorPolicyTest {

    @Test
    @DisplayName("Listener'ın sardığı OrderflowException'ın kodu DLT metriğinin code etiketi olur")
    void givenWrappedOrderflowException_whenErrorCode_thenErrorCodeOfException() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed",
                new DomainValidationException(SharedErrorCode.ID_REQUIRED, "orderId"));

        // When / Then
        assertThat(KafkaErrorPolicy.errorCode(exception)).isEqualTo("SHR_001");
    }

    @Test
    @DisplayName("Zincirde OrderflowException yoksa en içteki exception'ın adı kullanılır")
    void givenNoOrderflowException_whenErrorCode_thenRootCauseSimpleName() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed",
                new IllegalStateException("wrapper", new SQLTransientConnectionException("db down")));

        // When / Then
        assertThat(KafkaErrorPolicy.errorCode(exception)).isEqualTo("SQLTransientConnectionException");
    }

    @Test
    @DisplayName("Zincirde not-found ailesi varsa kısa retry: ilgili kayıt henüz gelmemiş olabilir")
    void givenWrappedNotFound_whenBackOffFor_thenShortRetry() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed", new TestResourceNotFoundException());

        // When / Then
        assertThat(KafkaErrorPolicy.backOffFor(exception)).isSameAs(KafkaErrorPolicy.NOT_FOUND_BACK_OFF);
    }

    @Test
    @DisplayName("Zincirde SQLState 23505 (unique ihlali) varsa 1–2 retry: idempotency yarışı")
    void givenWrappedUniqueViolation_whenBackOffFor_thenDuplicateKeyRetry() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed",
                new IllegalStateException("could not execute statement",
                        new SQLException("duplicate key value violates unique constraint", "23505")));

        // When / Then
        assertThat(KafkaErrorPolicy.backOffFor(exception)).isSameAs(KafkaErrorPolicy.UNIQUE_VIOLATION_BACK_OFF);
    }

    @Test
    @DisplayName("Başka bir SQL hatası ya da bilinmeyen hata varsayılan backoff'a kalır")
    void givenOtherFailure_whenBackOffFor_thenDefault() {
        // Given
        final Exception checkViolation = new ListenerExecutionFailedException("listener failed",
                new SQLException("new row violates check constraint", "23514"));
        final Exception unknown = new ListenerExecutionFailedException("listener failed", new IllegalStateException("boom"));

        // When / Then
        assertThat(KafkaErrorPolicy.backOffFor(checkViolation)).isNull();
        assertThat(KafkaErrorPolicy.backOffFor(unknown)).isNull();
    }
}
