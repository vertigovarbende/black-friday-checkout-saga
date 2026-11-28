package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.ReleaseStockUseCase;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.inbox.port.InboxMessage;
import com.deveyk.orderflow.shared.inbox.support.InboxProcessor;
import com.deveyk.orderflow.shared.messaging.EventJsonReader;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReleaseRequestedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Telafi C2. Cevap (stock.released) her durumda yayınlanır; reserve'den önce gelirse kapanış kaydı açılır.
@Component
@RequiredArgsConstructor
class StockReleaseRequestedKafkaListener {

    static final String CONSUMER = "product-service.stock-release-requested";

    private final EventJsonReader eventJsonReader;
    private final InboxProcessor inboxProcessor;
    private final UseCaseHandler<StockReservation, ReleaseStockUseCase> releaseStockUseCaseHandler;

    @KafkaListener(id = "stock-release-requested", idIsGroup = false, topics = TopicNames.STOCK_RELEASE_REQUESTED)
    public void onMessage(final String message) {

        final OrderflowEvent<StockReleaseRequestedPayload> event = eventJsonReader.read(message, StockReleaseRequestedPayload.class);
        final ReleaseStockUseCase useCase = StockReleaseRequestedPayloadToUseCaseMapper.toUseCase(event.payload());

        inboxProcessor.process(InboxMessage.of(CONSUMER, event.metadata()), () -> releaseStockUseCaseHandler.handle(useCase));

    }
}
