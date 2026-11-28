package com.deveyk.orderflow.order.adapter.in.messaging;

import ch.qos.logback.classic.Level;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderTestContainerConfiguration;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ProductChangedEventBuilder;
import com.deveyk.orderflow.shared.messaging.EventDefinitions;
import com.deveyk.orderflow.shared.messaging.TopicNames;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.OrderflowEvent;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.testsupport.messaging.InboxAssert;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

class ProductChangedKafkaListenerIT extends OrderTestContainerConfiguration {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    private ProductSnapshotReadPort productSnapshotReadPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Kopyası olmayan ürünün product.changed'i yeni kopya oluşturur; mesaj inbox'a kaydedilir")
    void givenUnknownProduct_whenProductChanged_thenSnapshotCreatedAndInboxRecorded() {
        // Given
        final UUID productId = UUID.randomUUID();
        final OrderflowEvent<ProductChangedPayload> event = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withProductVersion(1L)
                .build();

        // When
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, event);

        // Then
        final ProductSnapshot product = awaitProductVersion(productId, 1L);
        assertThat(product.getName()).isEqualTo(OrderValidTestData.Product.NAME);
        assertThat(product.getUnitPrice()).isEqualTo(OrderValidTestData.Product.UNIT_PRICE_MONEY);

        final InboxAssert.InboxRow inbox = InboxAssert.of(jdbcTemplate)
                .processedOnce(ProductChangedKafkaListener.CONSUMER, event.metadata().eventId());
        assertThat(inbox.eventType()).isEqualTo(EventDefinitions.PRODUCT_CHANGED.eventType());
        assertThat(inbox.aggregateId()).isEqualTo(productId.toString());
    }

    @Test
    @DisplayName("Daha yeni sürüm mevcut kopyayı günceller: ad, fiyat ve ürün sürümü")
    void givenExistingSnapshot_whenNewerVersionArrives_thenSnapshotUpdated() {
        // Given
        final UUID productId = UUID.randomUUID();
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withProductVersion(1L)
                .build());
        awaitProductVersion(productId, 1L);

        // When
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withName("Wireless Headphones Pro")
                .withUnitPrice(new MoneyAmount("279.90", "TRY"))
                .withProductVersion(2L)
                .build());

        // Then
        final ProductSnapshot product = awaitProductVersion(productId, 2L);
        assertThat(product.getName()).isEqualTo("Wireless Headphones Pro");
        assertThat(product.getUnitPrice()).isEqualTo(new Money(new BigDecimal("279.90"), TRY));
    }

    @Test
    @DisplayName("Eski sürüm (yeniden yayın, geç gelen kopya) işlenmiş sayılır ama kopyayı değiştirmez")
    void givenNewerSnapshot_whenOlderVersionArrives_thenSkippedButRecorded() {
        // Given
        final UUID productId = UUID.randomUUID();
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withName("Wireless Headphones Pro")
                .withProductVersion(2L)
                .build());
        awaitProductVersion(productId, 2L);
        final OrderflowEvent<ProductChangedPayload> older = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withName("Wireless Headphones")
                .withProductVersion(1L)
                .build();

        // When
        messagePublisher.publishEvent(TopicNames.PRODUCT_CHANGED, older);

        // Then
        awaitProcessed(older.metadata().eventId());
        final ProductSnapshot product = productSnapshotReadPort.findById(new ProductId(productId)).orElseThrow();
        assertThat(product.getName()).isEqualTo("Wireless Headphones Pro");
        assertThat(product.getProductVersion()).isEqualTo(2L);
        assertThat(logTracker.findMessage(Level.INFO, "Stale product change skipped productId=" + productId)).isPresent();
    }

    private ProductSnapshot awaitProductVersion(final UUID productId, final long productVersion) {
        return Awaitility.await("product %s at version %d".formatted(productId, productVersion))
                .atMost(TIMEOUT)
                .until(() -> productSnapshotReadPort.findById(new ProductId(productId)),
                        found -> found.filter(product -> product.getProductVersion() == productVersion).isPresent())
                .orElseThrow();
    }

    private void awaitProcessed(final UUID eventId) {
        Awaitility.await("inbox row for event %s".formatted(eventId))
                .atMost(TIMEOUT)
                .untilAsserted(() -> InboxAssert.of(jdbcTemplate).processedOnce(ProductChangedKafkaListener.CONSUMER, eventId));
    }
}
