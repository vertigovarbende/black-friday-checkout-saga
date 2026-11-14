package com.deveyk.orderflow.shared.domain;

import com.deveyk.orderflow.shared.domain.testsupport.SampleAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AggregateRootTest {

    @Test
    @DisplayName("Yeni oluşturulan aggregate'in sürümü 0'dır")
    void givenNewAggregate_whenGetVersion_thenZero() {
        // Given
        final SampleAggregate aggregate = SampleAggregate.create("id-1");

        // When
        final long version = aggregate.getVersion();

        // Then
        assertThat(version).isZero();
    }

    @Test
    @DisplayName("Rehydrate edilen aggregate kayıttaki sürümü taşır")
    void givenRehydratedAggregate_whenGetVersion_thenRestoredVersion() {
        // Given
        final SampleAggregate aggregate = SampleAggregate.rehydrate("id-1", 7L);

        // When
        final long version = aggregate.getVersion();

        // Then
        assertThat(version).isEqualTo(7L);
    }
}
