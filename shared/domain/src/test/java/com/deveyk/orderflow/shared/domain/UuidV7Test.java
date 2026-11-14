package com.deveyk.orderflow.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UuidV7Test {

    @Test
    @DisplayName("Üretilen UUID sürüm 7 ve RFC 9562 varyantındadır")
    void givenNothing_whenNext_thenVersion7AndRfcVariant() {
        // When
        final UUID uuid = UuidV7.next();

        // Then
        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);
    }

    @Test
    @DisplayName("UUID'nin ilk 48 biti verilen zamanı taşır")
    void givenTimestamp_whenNext_thenTimestampIsEmbedded() {
        // Given
        final UuidV7 generator = new UuidV7();
        final long epochMillis = 1_795_000_000_000L;

        // When
        final UUID uuid = generator.generate(epochMillis);

        // Then
        assertThat(UuidV7.timestampOf(uuid)).isEqualTo(epochMillis);
    }

    @Test
    @DisplayName("Aynı milisaniyede üretilen UUID'ler tekildir ve üretildikleri sırayla sıralanır")
    void givenSameMillisecond_whenNextManyTimes_thenUniqueAndOrdered() {
        // Given
        final UuidV7 generator = new UuidV7();
        final long epochMillis = 1_795_000_000_000L;
        final List<String> generated = new ArrayList<>();

        // When
        for (int i = 0; i < 10_000; i++) {
            generated.add(generator.generate(epochMillis).toString());
        }

        // Then
        assertThat(new HashSet<>(generated)).hasSize(generated.size());
        assertThat(generated).isSorted();
    }

    @Test
    @DisplayName("Saat geri gitse de UUID'ler sıralı kalır")
    void givenClockMovesBackwards_whenNext_thenStillOrdered() {
        // Given
        final UuidV7 generator = new UuidV7();
        final long later = 1_795_000_000_500L;
        final long earlier = 1_795_000_000_000L;

        // When
        final String first = generator.generate(later).toString();
        final String second = generator.generate(earlier).toString();

        // Then
        assertThat(second).isGreaterThan(first);
    }

    @Test
    @DisplayName("48 bite sığmayan zaman reddedilir")
    void givenTimestampOutOfRange_whenNext_thenThrow() {
        // Given
        final UuidV7 generator = new UuidV7();

        // When / Then
        assertThatThrownBy(() -> generator.generate(-1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> generator.generate(1L << 48))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
