package com.deveyk.orderflow.order.application.product.handler;

import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotSavePort;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ApplyProductChangeUseCaseBuilder;
import com.deveyk.orderflow.order.testsupport.builder.ProductSnapshotBuilder;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

class ApplyProductChangeUseCaseHandlerTest extends OrderflowUnitTest {

    private static final ProductId PRODUCT_ID = OrderValidTestData.Product.PRODUCT_ID;
    private static final String NEW_NAME = "Wireless Headphones Pro";
    private static final Money NEW_PRICE = new Money(new BigDecimal("279.90"), TRY);

    @Mock
    private ProductSnapshotReadPort productSnapshotReadPort;

    @Mock
    private ProductSnapshotSavePort productSnapshotSavePort;

    private ApplyProductChangeUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new ApplyProductChangeUseCaseHandler(productSnapshotReadPort, productSnapshotSavePort);
    }

    @Test
    @DisplayName("Kopyası olmayan ürün için yeni kopya oluşturulur")
    void givenUnknownProduct_whenHandle_thenSnapshotCreated() {
        // Given
        Mockito.when(productSnapshotReadPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When
        handler.handle(new ApplyProductChangeUseCaseBuilder()
                .withValidValues()
                .withName(NEW_NAME)
                .withUnitPrice(NEW_PRICE)
                .withProductVersion(1L)
                .build());

        // Then
        final ArgumentCaptor<ProductSnapshot> created = ArgumentCaptor.forClass(ProductSnapshot.class);
        Mockito.verify(productSnapshotSavePort).create(created.capture());
        assertThat(created.getValue().getId()).isEqualTo(PRODUCT_ID);
        assertThat(created.getValue().getName()).isEqualTo(NEW_NAME);
        assertThat(created.getValue().getUnitPrice()).isEqualTo(NEW_PRICE);
        assertThat(created.getValue().getProductVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Daha yeni sürüm mevcut kopyaya uygulanır ve kaydedilir")
    void givenOlderSnapshot_whenHandleNewerVersion_thenSnapshotUpdated() {
        // Given
        final ProductSnapshot existing = new ProductSnapshotBuilder().withValidValues().withProductVersion(1L).build();
        Mockito.when(productSnapshotReadPort.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));

        // When
        final ProductSnapshot result = handler.handle(new ApplyProductChangeUseCaseBuilder()
                .withValidValues()
                .withName(NEW_NAME)
                .withUnitPrice(NEW_PRICE)
                .withProductVersion(2L)
                .build());

        // Then
        Mockito.verify(productSnapshotSavePort).update(existing);
        assertThat(result.getProductVersion()).isEqualTo(2L);
        assertThat(result.getUnitPrice()).isEqualTo(NEW_PRICE);
    }

    @Test
    @DisplayName("Aynı ya da eski sürüm kaydedilmez; kopya olduğu gibi kalır")
    void givenNewerSnapshot_whenHandleOlderVersion_thenNothingSaved() {
        // Given
        final ProductSnapshot existing = new ProductSnapshotBuilder().withValidValues().withProductVersion(3L).build();
        Mockito.when(productSnapshotReadPort.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));

        // When
        final ProductSnapshot result = handler.handle(new ApplyProductChangeUseCaseBuilder()
                .withValidValues()
                .withName(NEW_NAME)
                .withUnitPrice(NEW_PRICE)
                .withProductVersion(2L)
                .build());

        // Then
        Mockito.verifyNoInteractions(productSnapshotSavePort);
        assertThat(result.getProductVersion()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo(OrderValidTestData.Product.NAME);
    }
}
