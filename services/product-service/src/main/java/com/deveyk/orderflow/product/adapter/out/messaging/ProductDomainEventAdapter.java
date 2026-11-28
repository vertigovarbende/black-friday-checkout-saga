package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.application.product.port.ProductDomainEventPort;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.shared.messaging.EventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.outbox.port.OutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ProductDomainEventAdapter implements ProductDomainEventPort {

    private final EventBuilder eventBuilder;
    private final OutboxEventPort outboxEventPort;

    @Override
    public void publishProductChanged(final Product product) {

        final OrderflowEvent<ProductChangedPayload> event = eventBuilder
                .newEvent(EventDefinitions.PRODUCT_CHANGED, product.getId().value().toString(), ProductToProductChangedPayloadMapper.toPayload(product))
                .withSource(EventSourceNames.PRODUCT_SERVICE)
                .build();

        outboxEventPort.append(EventDefinitions.PRODUCT_CHANGED.topic(), event);
    }
}
