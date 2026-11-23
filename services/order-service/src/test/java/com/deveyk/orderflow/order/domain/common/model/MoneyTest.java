package com.deveyk.orderflow.order.domain.common.model;

import com.deveyk.orderflow.order.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.EUR;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("Tutar para biriminin ondalık basamak sayısına getirilir; 10.5 ile 10.50 eşittir")
    void givenAmountsWithDifferentScales_whenCreate_thenNormalizedAndEqual() {
        // When
        final Money oneDecimal = new Money(new BigDecimal("10.5"), TRY);
        final Money threeDecimals = new Money(new BigDecimal("10.500"), TRY);

        // Then
        assertThat(oneDecimal).isEqualTo(threeDecimals);
        assertThat(oneDecimal.amount()).isEqualByComparingTo("10.50");
        assertThat(oneDecimal.amount().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Para biriminin izin verdiğinden fazla ondalık basamak yuvarlanmaz, reddedilir")
    void givenTooManyDecimals_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Money(new BigDecimal("10.555"), TRY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_SCALE_TOO_LARGE));
    }

    @Test
    @DisplayName("Ondalığı olmayan para biriminde (JPY) kesirli tutar reddedilir")
    void givenFractionalYen_whenCreate_thenRejected() {
        // Given
        final Currency jpy = Currency.getInstance("JPY");

        // When / Then
        assertThatThrownBy(() -> new Money(new BigDecimal("100.5"), jpy))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_SCALE_TOO_LARGE));
    }

    @Test
    @DisplayName("Negatif tutar reddedilir")
    void givenNegativeAmount_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Money(new BigDecimal("-1.00"), TRY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_NEGATIVE));
    }

    @Test
    @DisplayName("Tutarı ya da para birimi olmayan Money oluşturulamaz")
    void givenMissingParts_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Money(null, TRY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_REQUIRED));
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.CURRENCY_REQUIRED));
    }

    @Test
    @DisplayName("Aynı para birimindeki tutarlar toplanır")
    void givenSameCurrency_whenAdd_thenSum() {
        // Given
        final Money a = new Money(new BigDecimal("10.25"), TRY);
        final Money b = new Money(new BigDecimal("4.75"), TRY);

        // When
        final Money sum = a.add(b);

        // Then
        assertThat(sum).isEqualTo(new Money(new BigDecimal("15.00"), TRY));
    }

    @Test
    @DisplayName("Farklı para birimindeki tutarlar toplanamaz")
    void givenDifferentCurrencies_whenAdd_thenRejected() {
        // Given
        final Money lira = new Money(BigDecimal.TEN, TRY);
        final Money euro = new Money(BigDecimal.TEN, EUR);

        // When / Then
        assertThatThrownBy(() -> lira.add(euro))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.CURRENCY_MISMATCH));
    }

    @Test
    @DisplayName("Tutar adetle çarpılır")
    void givenQuantity_whenMultiply_thenProduct() {
        // Given
        final Money unitPrice = new Money(new BigDecimal("249.90"), TRY);

        // When
        final Money total = unitPrice.multiply(new Quantity(3));

        // Then
        assertThat(total).isEqualTo(new Money(new BigDecimal("749.70"), TRY));
    }

    @Test
    @DisplayName("Sıfır tutar geçerlidir ama pozitif değildir")
    void givenZero_whenIsPositive_thenFalse() {
        // When
        final Money zero = new Money(BigDecimal.ZERO, TRY);

        // Then
        assertThat(zero.isPositive()).isFalse();
        assertThat(new Money(BigDecimal.ONE, TRY).isPositive()).isTrue();
    }
}
