package com.deveyk.orderflow.shared.inbox;

public interface InboxEventPort {

    boolean tryRecord(InboxMessage message);
}
