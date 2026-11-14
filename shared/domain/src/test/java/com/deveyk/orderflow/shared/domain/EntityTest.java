package com.deveyk.orderflow.shared.domain;

import com.deveyk.orderflow.shared.domain.testsupport.OtherSampleEntity;
import com.deveyk.orderflow.shared.domain.testsupport.SampleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    @Test
    @DisplayName("Aynı sınıftan ve aynı kimlikli iki nesne eşittir, hash'leri de aynıdır")
    void givenSameClassAndSameId_whenCompare_thenEqualWithSameHashCode() {
        // Given
        final SampleEntity first = SampleEntity.of("id-1", "ilk");
        final SampleEntity second = SampleEntity.of("id-1", "ikinci");

        // When
        final boolean equal = first.equals(second);

        // Then
        assertThat(equal).isTrue();
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Kimlikleri farklı iki nesne eşit değildir")
    void givenDifferentIds_whenCompare_thenNotEqual() {
        // Given
        final SampleEntity first = SampleEntity.of("id-1", "aynı");
        final SampleEntity second = SampleEntity.of("id-2", "aynı");

        // When
        final boolean equal = first.equals(second);

        // Then
        assertThat(equal).isFalse();
    }

    @Test
    @DisplayName("Kimlikleri aynı ama sınıfları farklı iki nesne eşit değildir")
    void givenSameIdButDifferentClasses_whenCompare_thenNotEqual() {
        // Given
        final SampleEntity entity = SampleEntity.of("id-1", "x");
        final OtherSampleEntity other = OtherSampleEntity.of("id-1");

        // When
        final boolean equal = entity.equals(other);

        // Then
        assertThat(equal).isFalse();
    }

    @Test
    @DisplayName("Bir nesne null ile eşit değildir, kendisiyle eşittir")
    void givenNullOrSelf_whenCompare_thenOnlySelfIsEqual() {
        // Given
        final SampleEntity entity = SampleEntity.of("id-1", "x");

        // When / Then
        assertThat(entity.equals(null)).isFalse();
        assertThat(entity.equals(entity)).isTrue();
    }
}
