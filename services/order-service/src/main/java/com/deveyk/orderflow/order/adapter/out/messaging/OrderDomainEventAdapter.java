package com.deveyk.orderflow.order.adapter.out.messaging;

import com.deveyk.orderflow.order.application.order.port.OrderDomainEventPort;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.shared.messaging.AggregateTypeNames;
import com.deveyk.orderflow.shared.messaging.EventBuilder;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.EventTypeNames;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCreatedPayload;
import com.deveyk.orderflow.shared.outbox.OutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class OrderDomainEventAdapter implements OrderDomainEventPort {

    private final EventBuilder eventBuilder;
    private final OutboxEventPort outboxEventPort;

    @Override
    public void publishOrderCreated(final Order order) {

        final OrderflowEvent<OrderCreatedPayload> event = eventBuilder
                .newEvent(OrderToOrderCreatedPayloadMapper.toPayload(order))
                .withEventType(EventTypeNames.ORDER_CREATED)
                .withAggregate(AggregateTypeNames.ORDER, order.getId().value().toString())
                .withSource(EventSourceNames.ORDER_SERVICE)
                .build();

        outboxEventPort.append(TopicNames.ORDER_CREATED, event);
    }
}
