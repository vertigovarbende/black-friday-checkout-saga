package com.deveyk.orderflow.order.delivery;

import ch.qos.logback.classic.Level;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderDeliveryTestConfiguration;
import com.deveyk.orderflow.order.testsupport.builder.ProductChangedEventBuilder;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Teslim garantisi senaryosu 1 (07 §9.3): at-least-once teslimde aynı mesaj ikinci kez gelir; iş bir kez yapılır.
class DuplicateMessageDeliveryIT extends OrderDeliveryTestConfiguration {

    private static final String CONSUMER = "order-service.product-changed";
    private static final String DUPLICATE_COUNTER = "orderflow.inbox.duplicates";

    @Autowired
    private ProductSnapshotReadPort productSnapshotReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("Aynı product.changed iki kez gelir: kopya bir kez oluşturulur, ikincisi inbox'ta duplicate olarak atlanır")
    void givenSameMessageTwice_whenConsumed_thenProcessedOnceAndDuplicateSkipped() {
        // Given
        final UUID productId = UUID.randomUUID();
        final OrderflowEvent<ProductChangedPayload> event = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .build();
        final double duplicatesBefore = duplicates();

        // When
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, event);
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, event);

        // Then
        Awaitility.await("duplicate of event %s skipped".formatted(event.metadata().eventId()))
                .atMost(Duration.ofSeconds(30))
                .until(() -> duplicates() - duplicatesBefore, skipped -> skipped == 1.0);

        InboxAssert.of(jdbcTemplate).processedOnce(CONSUMER, event.metadata().eventId());
        final ProductSnapshot product = productSnapshotReadPort.findById(new ProductId(productId)).orElseThrow();
        assertThat(product.getVersion()).as("kopya bir kez yazıldı, hiç güncellenmedi").isZero();
        assertThat(logTracker.findMessage(Level.INFO, "Duplicate message skipped consumer=" + CONSUMER
                + " eventId=" + event.metadata().eventId())).isPresent();
    }

    private double duplicates() {
        final Counter counter = meterRegistry.find(DUPLICATE_COUNTER).tag("consumer", CONSUMER).counter();
        return counter == null ? 0.0 : counter.count();
    }
}
