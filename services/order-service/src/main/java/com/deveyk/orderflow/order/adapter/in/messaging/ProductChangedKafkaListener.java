package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.shared.application.UseCaseHandler;
import com.deveyk.orderflow.shared.inbox.InboxMessage;
import com.deveyk.orderflow.shared.inbox.InboxProcessor;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Ürün kopyasını product.changed ile günceller. Hata yakalanmaz; retry ve DLT Kafka error handler'ındadır
// (KafkaErrorHandlingConfiguration).
@Component
@RequiredArgsConstructor
class ProductChangedKafkaListener {

    static final String CONSUMER = "order-service.product-changed";

    private final EventJsonReader eventJsonReader;
    private final InboxProcessor inboxProcessor;
    private final UseCaseHandler<ProductSnapshot, ApplyProductChangeUseCase> applyProductChangeUseCaseHandler;

    // idIsGroup = false: id sadece container adıdır; group servisinkidir (spring.kafka.consumer.group-id, ADR-0018).
    @KafkaListener(id = "product-changed", idIsGroup = false, topics = TopicNames.PRODUCT_CHANGED)
    public void onMessage(final String message) {

        final OrderflowEvent<ProductChangedPayload> event = eventJsonReader.read(message, ProductChangedPayload.class);
        final ApplyProductChangeUseCase useCase = ProductChangedPayloadToUseCaseMapper.toUseCase(event.payload());

        inboxProcessor.process(InboxMessage.of(CONSUMER, event.metadata()), () -> applyProductChangeUseCaseHandler.handle(useCase));

    }
}
