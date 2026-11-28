package com.deveyk.orderflow.product.domain.common.model;

import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    @DisplayName("Negatif adet oluşturulamaz; sıfır stok sayacı olarak geçerlidir")
    void givenNegativeValue_whenCreate_thenRejected() {
        // When / Then
        assertThat(Quantity.ZERO.isPositive()).isFalse();
        assertThatThrownBy(() -> new Quantity(-1))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.QUANTITY_NEGATIVE));
    }

    @Test
    @DisplayName("Toplama ve çıkarma yeni bir adet döner; sonucu negatif olan çıkarma reddedilir")
    void givenQuantities_whenPlusAndMinus_thenNewQuantityOrRejected() {
        // Given
        final Quantity ten = new Quantity(10);
        final Quantity three = new Quantity(3);

        // When / Then
        assertThat(ten.plus(three)).isEqualTo(new Quantity(13));
        assertThat(ten.minus(three)).isEqualTo(new Quantity(7));
        assertThat(ten.isAtLeast(ten)).isTrue();
        assertThat(three.isAtLeast(ten)).isFalse();
        assertThatThrownBy(() -> three.minus(ten)).isInstanceOf(DomainValidationException.class);
    }
}
