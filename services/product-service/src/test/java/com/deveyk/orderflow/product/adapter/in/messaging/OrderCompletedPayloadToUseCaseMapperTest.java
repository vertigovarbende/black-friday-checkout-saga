package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCompletedPayloadToUseCaseMapperTest {

    @Test
    @DisplayName("orderId'si olmayan order.completed MSG_002 olur")
    void givenNoOrderId_whenToUseCase_thenFieldMissing() {
        // Given
        final OrderCompletedPayload payload = OrderCompletedPayload.builder().build();

        // When / Then
        assertThatThrownBy(() -> OrderCompletedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING))
                .hasMessageContaining("payload.orderId");
    }
}
