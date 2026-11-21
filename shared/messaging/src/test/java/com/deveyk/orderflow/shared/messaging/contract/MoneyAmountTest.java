package com.deveyk.orderflow.shared.messaging.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyAmountTest {

    private static final Currency TRY = Currency.getInstance("TRY");

    @Test
    @DisplayName("Tutar ölçeğiyle birlikte düz metin olarak, para birimi ISO koduyla yazılır")
    void givenAmountWithScale_whenOf_thenPlainStringKeepsScaleAndCurrencyCode() {
        // When
        final MoneyAmount moneyAmount = MoneyAmount.of(new BigDecimal("514.80"), TRY);

        // Then
        assertThat(moneyAmount.amount()).isEqualTo("514.80");
        assertThat(moneyAmount.currency()).isEqualTo("TRY");
    }

    @Test
    @DisplayName("Üslü gösterimdeki tutar bilimsel gösterim olmadan yazılır")
    void givenAmountInExponentForm_whenOf_thenPlainStringWithoutExponent() {
        // When
        final MoneyAmount moneyAmount = MoneyAmount.of(new BigDecimal("1E+3"), TRY);

        // Then
        assertThat(moneyAmount.amount()).isEqualTo("1000");
    }
}
