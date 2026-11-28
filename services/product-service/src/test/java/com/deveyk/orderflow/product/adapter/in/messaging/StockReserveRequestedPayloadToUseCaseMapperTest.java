package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.testsupport.builder.StockReserveRequestedEventBuilder;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockReserveRequestedPayloadToUseCaseMapperTest {

    @Test
    @DisplayName("lines alanı olmayan komut MSG_002 olur (retry'sız DLT)")
    void givenNoLines_whenToUseCase_thenFieldMissing() {
        // Given
        final StockReserveRequestedPayload payload = new StockReserveRequestedEventBuilder().withValidValues().withLines(null).build().payload();

        // When / Then
        assertThatThrownBy(() -> StockReserveRequestedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING))
                .hasMessageContaining("payload.lines");
    }

    @Test
    @DisplayName("Fiyatı olmayan satır, satırın yeriyle birlikte MSG_002 olur")
    void givenLineWithoutUnitPrice_whenToUseCase_thenFieldMissingWithIndex() {
        // Given
        final StockReserveRequestedPayload payload = new StockReserveRequestedEventBuilder().withValidValues()
                .withLines(List.of(StockReserveRequestedEventBuilder.line(UUID.randomUUID(), 1, MoneyAmount.of(BigDecimal.TEN, TRY)),
                        StockReserveRequestedEventBuilder.line(UUID.randomUUID(), 1, null)))
                .build().payload();

        // When / Then
        assertThatThrownBy(() -> StockReserveRequestedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING))
                .hasMessageContaining("payload.lines[1].unitPrice");
    }

    @Test
    @DisplayName("Sayı olmayan tutar MSG_003 olur")
    void givenUnparsableAmount_whenToUseCase_thenInvalidField() {
        // Given
        final StockReserveRequestedPayload payload = new StockReserveRequestedEventBuilder().withValidValues()
                .withLines(List.of(StockReserveRequestedEventBuilder.line(UUID.randomUUID(), 1, new MoneyAmount("abc", "TRY"))))
                .build().payload();

        // When / Then
        assertThatThrownBy(() -> StockReserveRequestedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.INVALID_EVENT_FIELD));
    }
}
