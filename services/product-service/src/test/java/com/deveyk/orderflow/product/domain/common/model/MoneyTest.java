package com.deveyk.orderflow.product.domain.common.model;

import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("Tutar para biriminin ölçeğine getirilir: 249.9 ile 249.90 eşittir (fiyat kontrolü buna dayanır)")
    void givenDifferentScales_whenCreate_thenEqual() {
        // When / Then
        assertThat(new Money(new BigDecimal("249.9"), TRY)).isEqualTo(new Money(new BigDecimal("249.90"), TRY));
    }

    @Test
    @DisplayName("Para biriminin izin verdiğinden fazla ondalık reddedilir")
    void givenTooManyDecimals_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Money(new BigDecimal("249.901"), TRY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_SCALE_TOO_LARGE));
    }

    @Test
    @DisplayName("Negatif tutar reddedilir")
    void givenNegativeAmount_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), TRY))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.AMOUNT_NEGATIVE));
    }
}
