package com.deveyk.orderflow.order.application.order.handler;

import com.deveyk.orderflow.order.application.order.port.OrderDomainEventPort;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.application.product.port.ProductSnapshotReadPort;
import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.exception.ProductNotAvailableException;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.domain.product.model.ProductSnapshot;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.ProductSnapshotBuilder;
import com.deveyk.orderflow.order.testsupport.fake.InMemoryOrderAdapter;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import com.deveyk.orderflow.shared.testsupport.time.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class PlaceOrderUseCaseHandlerTest extends OrderflowUnitTest {

    private static final CustomerId CUSTOMER = new CustomerId(OrderValidTestData.Customer.ID);

    private static final ProductSnapshot HEADPHONES = new ProductSnapshotBuilder()
            .withValidValues()
            .build();

    private static final ProductSnapshot CABLE = new ProductSnapshotBuilder()
            .withValidValues()
            .withId(new ProductId(OrderValidTestData.SecondProduct.ID))
            .withName(OrderValidTestData.SecondProduct.NAME)
            .withUnitPrice(new Money(OrderValidTestData.SecondProduct.UNIT_PRICE, TRY))
            .build();

    @Mock
    private ProductSnapshotReadPort productSnapshotReadPort;
    @Mock
    private OrderDomainEventPort orderDomainEventPort;
    private final InMemoryOrderAdapter orderAdapter = new InMemoryOrderAdapter();
    private final MutableClock clock = MutableClock.at(NOW);
    private PlaceOrderUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new PlaceOrderUseCaseHandler(productSnapshotReadPort, orderAdapter, orderDomainEventPort, clock);
    }

    @Test
    @DisplayName("Satırların adı ve fiyatı ürün kopyasından alınır; sipariş PENDING olarak kaydedilir")
    void givenKnownProducts_whenHandle_thenOrderPlacedWithSnapshotNameAndPrice() {
        // Given
        final PlaceOrderUseCase useCase = new PlaceOrderUseCase(CUSTOMER, List.of(
                new PlaceOrderUseCase.Line(HEADPHONES.getId(), new Quantity(2)),
                new PlaceOrderUseCase.Line(CABLE.getId(), new Quantity(1))
        ));
        Mockito.when(productSnapshotReadPort.findAllByIds(Set.of(HEADPHONES.getId(), CABLE.getId())))
                .thenReturn(List.of(CABLE, HEADPHONES));

        // When
        final Order placed = handler.handle(useCase);

        // Then
        assertThat(placed.getCustomerId()).isEqualTo(CUSTOMER);
        assertThat(placed.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(placed.getPlacedAt()).isEqualTo(NOW);
        assertThat(placed.getTotal()).isEqualTo(new Money(new BigDecimal("514.80"), TRY));
        assertThat(placed.getLines())
                .extracting(OrderLine::getProductId, OrderLine::getProductName, OrderLine::getUnitPrice, OrderLine::getQuantity)
                .containsExactly(
                        tuple(HEADPHONES.getId(), HEADPHONES.getName(), HEADPHONES.getUnitPrice(), new Quantity(2)),
                        tuple(CABLE.getId(), CABLE.getName(), CABLE.getUnitPrice(), new Quantity(1))
                );
        assertThat(orderAdapter.findById(placed.getId())).contains(placed);

        // Verify
        Mockito.verify(productSnapshotReadPort, Mockito.times(1))
                .findAllByIds(Set.of(HEADPHONES.getId(), CABLE.getId()));
        Mockito.verify(orderDomainEventPort, Mockito.times(1)).publishOrderCreated(placed);
    }

    @Test
    @DisplayName("Kopyada olmayan ürün sipariş edilemez; eksik ürünler hatada listelenir ve hiçbir şey kaydedilmez")
    void givenUnknownProduct_whenHandle_thenProductNotAvailableAndNothingSaved() {
        // Given
        final ProductId unknown = new ProductId(UUID.fromString("0198a1b2-0000-7000-8000-00000000dead"));
        final PlaceOrderUseCase useCase = new PlaceOrderUseCase(CUSTOMER, List.of(
                new PlaceOrderUseCase.Line(HEADPHONES.getId(), new Quantity(1)),
                new PlaceOrderUseCase.Line(unknown, new Quantity(1))
        ));
        Mockito.when(productSnapshotReadPort.findAllByIds(Set.of(HEADPHONES.getId(), unknown)))
                .thenReturn(List.of(HEADPHONES));

        // When / Then
        assertThatThrownBy(() -> handler.handle(useCase))
                .isInstanceOfSatisfying(ProductNotAvailableException.class, e -> {
                    assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.PRODUCT_NOT_AVAILABLE);
                    assertThat(e.getMessage()).contains(unknown.value().toString())
                            .doesNotContain(HEADPHONES.getId().value().toString());
                });
        assertThat(orderAdapter.storedCount()).isZero();

        // Verify
        Mockito.verifyNoInteractions(orderDomainEventPort);
    }

    @Test
    @DisplayName("Sipariş zamanı handler'a verilen saatten alınır")
    void givenAdvancedClock_whenHandle_thenPlacedAtFromClock() {
        // Given
        clock.advance(Duration.ofMinutes(5));
        final PlaceOrderUseCase useCase = new PlaceOrderUseCase(CUSTOMER, List.of(
                new PlaceOrderUseCase.Line(HEADPHONES.getId(), new Quantity(1))
        ));
        Mockito.when(productSnapshotReadPort.findAllByIds(Set.of(HEADPHONES.getId())))
                .thenReturn(List.of(HEADPHONES));

        // When
        final Order placed = handler.handle(useCase);

        // Then
        assertThat(placed.getPlacedAt()).isEqualTo(NOW.plusSeconds(300));
        assertThat(placed.getLastModifiedAt()).isEqualTo(NOW.plusSeconds(300));
    }
}
