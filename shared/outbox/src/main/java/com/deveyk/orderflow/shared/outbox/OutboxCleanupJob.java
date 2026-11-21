package com.deveyk.orderflow.shared.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
class OutboxCleanupJob {

    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;
    private final Duration retention;

    OutboxCleanupJob(final OutboxEventRepository outboxEventRepository,
                     final Clock clock,
                     final Duration retention) {

        this.outboxEventRepository = outboxEventRepository;
        this.clock = clock;
        this.retention = retention;

    }

    @Scheduled(
            fixedDelayString = "${orderflow.outbox.cleanup.interval:PT1H}",
            initialDelayString = "${orderflow.outbox.cleanup.interval:PT1H}"
    )
    @Transactional
    public int cleanup() {

        final Instant cutoff = clock.instant().minus(retention);
        final int deleted = outboxEventRepository.deleteCreatedBefore(cutoff);

        if (deleted > 0) {
            log.info("Outbox cleanup deleted={} olderThan={}", deleted, cutoff);
        }

        return deleted;
    }
}
