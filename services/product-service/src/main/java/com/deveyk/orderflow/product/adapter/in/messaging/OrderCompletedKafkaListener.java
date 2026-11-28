package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.CommitStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.inbox.port.InboxMessage;
import com.deveyk.orderflow.shared.inbox.support.InboxProcessor;
import com.deveyk.orderflow.shared.messaging.EventJsonReader;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.order.OrderCompletedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Sipariş tamamlandı: rezervasyon kesinleşir (COMMITTED). Saga'ya cevap yoktur (05 §8).
@Component
@RequiredArgsConstructor
class OrderCompletedKafkaListener {

    static final String CONSUMER = "product-service.order-completed";

    private final EventJsonReader eventJsonReader;
    private final InboxProcessor inboxProcessor;
    private final UseCaseHandler<StockReservation, CommitStockUseCase> commitStockUseCaseHandler;

    @KafkaListener(id = "order-completed", idIsGroup = false, topics = TopicNames.ORDER_COMPLETED)
    public void onMessage(final String message) {

        final OrderflowEvent<OrderCompletedPayload> event = eventJsonReader.read(message, OrderCompletedPayload.class);
        final CommitStockUseCase useCase = OrderCompletedPayloadToUseCaseMapper.toUseCase(event.payload());

        inboxProcessor.process(InboxMessage.of(CONSUMER, event.metadata()), () -> commitStockUseCaseHandler.handle(useCase));

    }
}
