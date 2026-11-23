package com.deveyk.orderflow.shared.inbox.cleanup;

import com.deveyk.orderflow.shared.inbox.jpa.repository.InboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class InboxCleanupJob {

    private final InboxEventRepository inboxEventRepository;
    private final Clock clock;
    private final Duration retention;

    public InboxCleanupJob(final InboxEventRepository inboxEventRepository,
                           final Clock clock,
                           final Duration retention) {

        this.inboxEventRepository = inboxEventRepository;
        this.clock = clock;
        this.retention = retention;
    }

    @Scheduled(
            fixedDelayString = "${orderflow.inbox.cleanup.interval:PT1H}",
            initialDelayString = "${orderflow.inbox.cleanup.interval:PT1H}"
    )
    @Transactional
    public int cleanup() {

        final Instant cutoff = clock.instant().minus(retention);
        final int deleted = inboxEventRepository.deleteProcessedBefore(cutoff);

        if (deleted > 0) {
            log.info("Inbox cleanup deleted={} olderThan={}", deleted, cutoff);
        }

        return deleted;
    }
}
