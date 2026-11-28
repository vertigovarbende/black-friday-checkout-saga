package com.deveyk.orderflow.product.adapter.out.messaging;

import com.deveyk.orderflow.product.application.reservation.port.StockSagaEventPort;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.messaging.EventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinition;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.EventSourceNames;
import com.deveyk.orderflow.shared.outbox.port.OutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Saga'ya stok cevapları; key = aggregateId = orderId (event-catalog.md §1).
@Component
@RequiredArgsConstructor
class StockSagaEventAdapter implements StockSagaEventPort {

    private final EventBuilder eventBuilder;
    private final OutboxEventPort outboxEventPort;

    @Override
    public void publishStockReserved(final StockReservation reservation) {
        append(EventDefinitions.STOCK_RESERVED, reservation.getId(), StockReservationToPayloadMapper.toReservedPayload(reservation));
    }

    @Override
    public void publishStockReserveFailed(final OrderId orderId, final StockRejection rejection) {
        append(EventDefinitions.STOCK_RESERVE_FAILED, orderId, StockReservationToPayloadMapper.toReserveFailedPayload(orderId, rejection));
    }

    @Override
    public void publishStockReleased(final StockReservation reservation) {
        append(EventDefinitions.STOCK_RELEASED, reservation.getId(), StockReservationToPayloadMapper.toReleasedPayload(reservation));
    }

    private void append(final EventDefinition definition, final OrderId orderId, final Object payload) {
        outboxEventPort.append(definition.topic(), eventBuilder
                .newEvent(definition, orderId.value().toString(), payload)
                .withSource(EventSourceNames.PRODUCT_SERVICE)
                .build());
    }
}
