package com.deveyk.orderflow.order.config;

import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.ListenerExecutionFailedException;

import java.sql.SQLTransientConnectionException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorHandlingConfigurationTest {

    @Test
    @DisplayName("Listener'ın sardığı OrderflowException'ın kodu DLT metriğinin code etiketi olur")
    void givenWrappedOrderflowException_whenErrorCode_thenErrorCodeOfException() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed",
                new DomainValidationException(ProductSnapshotErrorCode.PRODUCT_NAME_REQUIRED));

        // When / Then
        assertThat(KafkaErrorHandlingConfiguration.errorCode(exception)).isEqualTo("ORD_202");
    }

    @Test
    @DisplayName("Zincirde OrderflowException yoksa en içteki exception'ın adı kullanılır")
    void givenNoOrderflowException_whenErrorCode_thenRootCauseSimpleName() {
        // Given
        final Exception exception = new ListenerExecutionFailedException("listener failed",
                new IllegalStateException("wrapper", new SQLTransientConnectionException("db down")));

        // When / Then
        assertThat(KafkaErrorHandlingConfiguration.errorCode(exception)).isEqualTo("SQLTransientConnectionException");
    }
}
