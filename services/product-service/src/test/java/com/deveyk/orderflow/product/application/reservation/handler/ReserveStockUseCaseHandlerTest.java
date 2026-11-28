package com.deveyk.orderflow.product.application.reservation.handler;

import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationReadPort;
import com.deveyk.orderflow.product.application.reservation.port.StockReservationSavePort;
import com.deveyk.orderflow.product.application.reservation.port.StockSagaEventPort;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RejectionReason;
import com.deveyk.orderflow.product.domain.reservation.model.ReservationStatus;
import com.deveyk.orderflow.product.domain.reservation.model.StockRejection;
import com.deveyk.orderflow.product.domain.reservation.model.StockReservation;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.product.testsupport.builder.ReserveStockUseCaseBuilder;
import com.deveyk.orderflow.product.testsupport.builder.StockReservationBuilder;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReserveStockUseCaseHandlerTest extends OrderflowUnitTest {

    private static final OrderId ORDER_ID = ProductValidTestData.Order.ORDER_ID;
    private static final ProductId PRODUCT_ID = ProductValidTestData.Product.PRODUCT_ID;

    @Mock
    private StockReservationReadPort stockReservationReadPort;

    @Mock
    private StockReservationSavePort stockReservationSavePort;

    @Mock
    private ProductReadPort productReadPort;

    @Mock
    private ProductSavePort productSavePort;

    @Mock
    private StockSagaEventPort stockSagaEventPort;

    private ReserveStockUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new ReserveStockUseCaseHandler(stockReservationReadPort, stockReservationSavePort, productReadPort,
                productSavePort, stockSagaEventPort, Clock.fixed(ProductValidTestData.NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Stok yeterliyse rezervasyon RESERVED kaydedilir, ürünün stoğu ayrılır ve stock.reserved yayınlanır")
    void givenEnoughStock_whenHandle_thenReservedSavedAndReplied() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(10).build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.empty());
        Mockito.when(productReadPort.findAllByIdsAsMap(List.of(PRODUCT_ID))).thenReturn(Map.of(PRODUCT_ID, product));

        // When
        final StockReservation reservation = handler.handle(new ReserveStockUseCaseBuilder().withValidValues().build());

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        Mockito.verify(stockReservationSavePort).create(reservation);
        Mockito.verify(productSavePort).update(product);
        assertThat(product.getReserved()).isEqualTo(ProductValidTestData.Order.QUANTITY);
        Mockito.verify(stockSagaEventPort).publishStockReserved(reservation);
        Mockito.verifyNoMoreInteractions(stockSagaEventPort);
    }

    @Test
    @DisplayName("Stok yetmezse REJECTED kaydedilir, stok değişmez ve stock.reserve.failed (INSUFFICIENT_STOCK) yayınlanır")
    void givenNotEnoughStock_whenHandle_thenRejectedSavedAndFailureReplied() {
        // Given
        final Product product = new ProductBuilder().withValidValues().withOnHand(1).build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.empty());
        Mockito.when(productReadPort.findAllByIdsAsMap(List.of(PRODUCT_ID))).thenReturn(Map.of(PRODUCT_ID, product));

        // When
        final StockReservation reservation = handler.handle(new ReserveStockUseCaseBuilder().withValidValues().build());

        // Then
        final ArgumentCaptor<StockReservation> created = ArgumentCaptor.forClass(StockReservation.class);
        Mockito.verify(stockReservationSavePort).create(created.capture());
        assertThat(created.getValue().getStatus()).isEqualTo(ReservationStatus.REJECTED);
        assertThat(product.getReserved()).isEqualTo(Quantity.ZERO);
        Mockito.verify(stockSagaEventPort).publishStockReserveFailed(ORDER_ID, new StockRejection(RejectionReason.INSUFFICIENT_STOCK, PRODUCT_ID));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
    }

    @Test
    @DisplayName("Rezervasyon zaten varsa (aynı niyet, yeni eventId) hiçbir şey değişmez, cevap yeniden yayınlanır")
    void givenExistingReservation_whenHandle_thenReplyPublishedAgainWithoutChanges() {
        // Given
        final StockReservation existing = new StockReservationBuilder().withValidValues().build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.of(existing));

        // When
        handler.handle(new ReserveStockUseCaseBuilder().withValidValues().build());

        // Then
        Mockito.verify(stockSagaEventPort).publishStockReserved(existing);
        Mockito.verifyNoInteractions(stockReservationSavePort, productReadPort, productSavePort);
    }

    @Test
    @DisplayName("Release reserve'den önce geldiyse (kapanış kaydı) stoğa dokunulmaz, RESERVATION_CLOSED ile reddedilir")
    void givenClosingRecord_whenHandle_thenReservationClosedWithoutTouchingStock() {
        // Given
        final StockReservation closed = new StockReservationBuilder().withValidValues().withClosingRecord().build();
        Mockito.when(stockReservationReadPort.findById(ORDER_ID)).thenReturn(Optional.of(closed));

        // When
        handler.handle(new ReserveStockUseCaseBuilder().withValidValues().build());

        // Then
        Mockito.verify(stockSagaEventPort).publishStockReserveFailed(ORDER_ID, StockRejection.reservationClosed());
        Mockito.verifyNoInteractions(stockReservationSavePort, productReadPort, productSavePort);
    }
}
