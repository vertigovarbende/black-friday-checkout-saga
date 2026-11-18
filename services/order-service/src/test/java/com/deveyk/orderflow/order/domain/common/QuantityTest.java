package com.deveyk.orderflow.order.domain.common;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    @DisplayName("Negatif adet reddedilir")
    void givenNegativeValue_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new Quantity(-1))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.QUANTITY_NEGATIVE));
    }

    @Test
    @DisplayName("Sıfır adet geçerlidir ama pozitif değildir; 1 ve üstü pozitiftir")
    void givenZeroAndOne_whenIsPositive_thenOnlyOneIsPositive() {
        // When
        final Quantity zero = new Quantity(0);
        final Quantity one = new Quantity(1);

        // Then
        assertThat(zero.isPositive()).isFalse();
        assertThat(one.isPositive()).isTrue();
    }
}
