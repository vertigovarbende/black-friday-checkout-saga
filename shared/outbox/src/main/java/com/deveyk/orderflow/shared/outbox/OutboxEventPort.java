package com.deveyk.orderflow.shared.outbox;

import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;

// shared-outbox'ın port'u: servisin application port'u değil, servisin event adapter'ları kullanır (01 §3.2).
public interface OutboxEventPort {

    void append(String topic, OrderflowEvent<?> event);
}
