package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleaseRequestedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockReleaseRequestedPayloadToUseCaseMapperTest {

    @Test
    @DisplayName("orderId'si olmayan komut MSG_002 olur")
    void givenNoOrderId_whenToUseCase_thenFieldMissing() {
        // When / Then
        assertThatThrownBy(() -> StockReleaseRequestedPayloadToUseCaseMapper.toUseCase(new StockReleaseRequestedPayload(null)))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING))
                .hasMessageContaining("payload.orderId");
    }
}
