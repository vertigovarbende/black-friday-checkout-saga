package com.deveyk.orderflow.order.domain.order.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} → {1}: {2}")
    @DisplayName("Sadece PENDING'den COMPLETED ya da CANCELLED'a geçilebilir")
    @CsvSource({
            "PENDING,   PENDING,   false",
            "PENDING,   COMPLETED, true",
            "PENDING,   CANCELLED, true",
            "COMPLETED, PENDING,   false",
            "COMPLETED, COMPLETED, false",
            "COMPLETED, CANCELLED, false",
            "CANCELLED, PENDING,   false",
            "CANCELLED, COMPLETED, false",
            "CANCELLED, CANCELLED, false"
    })
    void givenStatuses_whenCanTransitionTo_thenMatchesTable(final OrderStatus from, final OrderStatus to,
                                                             final boolean allowed) {
        // When
        final boolean result = from.canTransitionTo(to);

        // Then
        assertThat(result).isEqualTo(allowed);
    }
}
