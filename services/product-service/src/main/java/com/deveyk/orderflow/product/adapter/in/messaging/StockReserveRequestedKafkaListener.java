package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.ReserveStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.inbox.port.InboxMessage;
import com.deveyk.orderflow.shared.inbox.support.InboxProcessor;
import com.deveyk.orderflow.shared.messaging.EventJsonReader;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Saga T2. Cevap (stock.reserved | stock.reserve.failed) handler'da, inbox'ın transaction'ında outbox'a yazılır.
@Component
@RequiredArgsConstructor
class StockReserveRequestedKafkaListener {

    static final String CONSUMER = "product-service.stock-reserve-requested";

    private final EventJsonReader eventJsonReader;
    private final InboxProcessor inboxProcessor;
    private final UseCaseHandler<StockReservation, ReserveStockUseCase> reserveStockUseCaseHandler;

    @KafkaListener(id = "stock-reserve-requested", idIsGroup = false, topics = TopicNames.STOCK_RESERVE_REQUESTED)
    public void onMessage(final String message) {

        final OrderflowEvent<StockReserveRequestedPayload> event = eventJsonReader.read(message, StockReserveRequestedPayload.class);
        final ReserveStockUseCase useCase = StockReserveRequestedPayloadToUseCaseMapper.toUseCase(event.payload());

        inboxProcessor.process(InboxMessage.of(CONSUMER, event.metadata()), () -> reserveStockUseCaseHandler.handle(useCase));

    }
}
