package com.deveyk.orderflow.shared.inbox.adapter;

import com.deveyk.orderflow.shared.inbox.jpa.repository.InboxEventRepository;
import com.deveyk.orderflow.shared.inbox.port.InboxEventPort;
import com.deveyk.orderflow.shared.inbox.port.InboxMessage;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

public class InboxEventAdapter implements InboxEventPort {

    private final InboxEventRepository inboxEventRepository;
    private final Clock clock;

    public InboxEventAdapter(final InboxEventRepository inboxEventRepository, final Clock clock) {

        this.inboxEventRepository = inboxEventRepository;
        this.clock = clock;
    }

    // MANDATORY: kendi transaction'ını açsaydı kayıt işten bağımsız commit olurdu; iş hata verince mesaj işlenmiş sayılırdı.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryRecord(final InboxMessage message) {

        return inboxEventRepository.insertIfAbsent(message.consumer(), message.eventId(), message.eventType(), message.aggregateId(), clock.instant()) == 1;

    }
}
