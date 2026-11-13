package com.deveyk.orderflow.shared.persistence;

import com.deveyk.orderflow.shared.persistence.testsupport.MutableClock;
import com.deveyk.orderflow.shared.persistence.testsupport.ProbeEntity;
import com.deveyk.orderflow.shared.persistence.testsupport.ProbeRepository;
import com.deveyk.orderflow.shared.persistence.testsupport.SharedPersistenceTestContainerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseEntityIT extends SharedPersistenceTestContainerConfiguration {

    @Autowired
    private ProbeRepository probeRepository;

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void resetClock() {
        clock.setTo(SharedPersistenceTestApplication.NOW);
    }

    @Test
    @DisplayName("İlk kayıtta versiyon 0 olur, createdAt ve updatedAt uygulamanın saatinden gelir")
    void givenNewEntity_whenSave_thenVersionZeroAndAuditTimesFromClock() {
        // Given
        final UUID id = UUID.randomUUID();

        // When
        probeRepository.save(new ProbeEntity(id, "first"));

        // Then
        final ProbeEntity saved = probeRepository.findById(id).orElseThrow();
        assertThat(saved.getVersion()).isZero();
        assertThat(saved.getCreatedAt()).isEqualTo(SharedPersistenceTestApplication.NOW);
        assertThat(saved.getUpdatedAt()).isEqualTo(SharedPersistenceTestApplication.NOW);
    }

    @Test
    @DisplayName("Güncellemede versiyon artar, updatedAt yeni saati alır, createdAt değişmez")
    void givenSavedEntity_whenUpdateLater_thenVersionIncrementsAndOnlyUpdatedAtChanges() {
        // Given
        final UUID id = UUID.randomUUID();
        probeRepository.save(new ProbeEntity(id, "first"));
        final ProbeEntity loaded = probeRepository.findById(id).orElseThrow();
        clock.advance(Duration.ofMinutes(5));
        final Instant later = SharedPersistenceTestApplication.NOW.plus(Duration.ofMinutes(5));

        // When
        loaded.rename("second");
        probeRepository.save(loaded);

        // Then
        final ProbeEntity updated = probeRepository.findById(id).orElseThrow();
        assertThat(updated.getName()).isEqualTo("second");
        assertThat(updated.getVersion()).isEqualTo(1L);
        assertThat(updated.getCreatedAt()).isEqualTo(SharedPersistenceTestApplication.NOW);
        assertThat(updated.getUpdatedAt()).isEqualTo(later);
    }

    @Test
    @DisplayName("Eski versiyonla yapılan güncelleme optimistic lock hatası verir ve satır değişmez")
    void givenStaleCopy_whenSave_thenOptimisticLockingFailure() {
        // Given
        final UUID id = UUID.randomUUID();
        probeRepository.save(new ProbeEntity(id, "first"));
        final ProbeEntity first = probeRepository.findById(id).orElseThrow();
        final ProbeEntity stale = probeRepository.findById(id).orElseThrow();
        first.rename("second");
        probeRepository.save(first);

        // When
        stale.rename("third");

        // Then
        assertThatThrownBy(() -> probeRepository.save(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
        final ProbeEntity current = probeRepository.findById(id).orElseThrow();
        assertThat(current.getName()).isEqualTo("second");
        assertThat(current.getVersion()).isEqualTo(1L);
    }
}
