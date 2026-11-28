package com.deveyk.orderflow.product.application.reservation.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.application.reservation.usecase.CommitStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationNotFoundException;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommitStockUseCaseHandlerTest extends OrderflowUnitTest {

    private static final OrderId ORDER_ID = ProductValidTestData.Order.ORDER_ID;
    private static final ProductId PRODUCT_ID = ProductValidTestData.Product.PRODUCT_ID;
    private static final CommitStockUseCase USE_CASE = new CommitStockUseCase(ORDER_ID);

    @Mock
    private StockReservationReadPort stockReservationReadPort;

    @Mock
    private StockReservationSavePort stockReservationSavePort;

    @Mock
    private ProductReadPort productReadPort;

    @Mock
    private ProductSavePort productSavePort;

    private CommitStockUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new CommitStockUseCaseHandler(stockReservationReadPort, stockReservationSavePort, productReadPort,
                productSavePort, Clock.fixed(ProductValidTestData.LATER, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("RESERVED rezervasyon commit edilir ve ayrılan stok depodan düşer")
    void givenReserved_whenHandle_thenCommittedAndStockDeducted() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).withReserved(2).build();
        final StockReservation reservation = new StockReservationBuilder().withValidValues().build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.of(reservation));
        Mockito.when(productReadPort.findAllByIdsAsMap(Set.of(PRODUCT_ID))).thenReturn(Map.of(PRODUCT_ID, product));

        // When
        handler.handle(USE_CASE);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMMITTED);
        assertThat(product.getOnHand()).isEqualTo(new Quantity(8));
        Mockito.verify(stockReservationSavePort).update(reservation);
        Mockito.verify(productSavePort).update(product);
    }

    @Test
    @DisplayName("Zaten COMMITTED ise hiçbir şey kaydedilmez")
    void givenCommitted_whenHandle_thenNothingSaved() {
        // Given
        final StockReservation reservation = new StockReservationBuilder().withValidValues().withStatus(ReservationStatus.COMMITTED).build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.of(reservation));
        Mockito.when(productReadPort.findAllByIdsAsMap(Set.of(PRODUCT_ID))).thenReturn(Map.of());

        // When
        handler.handle(USE_CASE);

        // Then
        Mockito.verifyNoInteractions(stockReservationSavePort, productSavePort);
    }

    @Test
    @DisplayName("Rezervasyon yoksa not-found (Kafka'da kısa retry, sonra DLT)")
    void givenNoReservation_whenHandle_thenNotFound() {
        // Given
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> handler.handle(USE_CASE)).isInstanceOf(StockReservationNotFoundException.class);
    }
}
