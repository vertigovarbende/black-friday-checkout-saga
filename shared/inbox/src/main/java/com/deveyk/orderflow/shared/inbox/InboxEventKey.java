package com.deveyk.orderflow.shared.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
record InboxEventKey(@Column(name = "consumer", nullable = false, length = 100)
                     String consumer,
                     @Column(name = "event_id", nullable = false)
                     UUID eventId) implements Serializable {


}
