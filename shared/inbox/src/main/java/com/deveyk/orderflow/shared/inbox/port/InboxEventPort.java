package com.deveyk.orderflow.shared.inbox.port;

public interface InboxEventPort {

    boolean tryRecord(InboxMessage message);
}
