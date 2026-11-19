package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.adapter.in.web.response.MoneyResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderLineResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.common.Money;
import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

class OrderWebMapperTest {

    private static final UUID USER_ID = OrderValidTestData.Customer.ID;

    private final OrderWebMapper mapper = Mappers.getMapper(OrderWebMapper.class);

    @Test
    @DisplayName("Sipariş isteği use case'e çevrilir: X-User-Id müşteri olur, satırların sırası korunur")
    void givenPlaceOrderRequest_whenToPlaceOrderUseCase_thenCustomerAndLinesMapped() {
        // Given
        final UUID first = OrderValidTestData.Product.ID;
        final UUID second = OrderValidTestData.SecondProduct.ID;
        final PlaceOrderRequest request = new PlaceOrderRequest(List.of(
                new PlaceOrderRequest.Line(first, 2),
                new PlaceOrderRequest.Line(second, 1)));

        // When
        final PlaceOrderUseCase useCase = mapper.toPlaceOrderUseCase(USER_ID, request);

        // Then
        assertThat(useCase).isEqualTo(new PlaceOrderUseCase(new CustomerId(USER_ID), List.of(
                new PlaceOrderUseCase.Line(new ProductId(first), new Quantity(2)),
                new PlaceOrderUseCase.Line(new ProductId(second), new Quantity(1)))));
    }

    @Test
    @DisplayName("Sipariş okuma isteği use case'e çevrilir")
    void givenUserAndOrderId_whenToGetOrderUseCase_thenMapped() {
        // Given
        final UUID orderId = OrderId.newId().value();

        // When
        final GetOrderUseCase useCase = mapper.toGetOrderUseCase(USER_ID, orderId);

        // Then
        assertThat(useCase).isEqualTo(new GetOrderUseCase(new OrderId(orderId), new CustomerId(USER_ID)));
    }

    @Test
    @DisplayName("Yeni sipariş cevabı id ve durumu taşır")
    void givenPlacedOrder_whenToPlaceOrderResponse_thenIdAndStatus() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When
        final PlaceOrderResponse response = mapper.toPlaceOrderResponse(order);

        // Then
        assertThat(response).isEqualTo(new PlaceOrderResponse(order.getId().value(), "PENDING"));
    }

    @Test
    @DisplayName("Sipariş cevabında tutarlar string, ondalık hane sayısı korunur; satır ve sipariş toplamı hesaplanır")
    void givenOrderWithTwoLines_whenToOrderResponse_thenAmountsAsStringsAndTotals() {
        // Given
        final OrderLine headphones = new OrderLineBuilder()
                .withValidValues()
                .withQuantity(new Quantity(2))
                .build();
        final OrderLine cable = new OrderLineBuilder()
                .withValidValues()
                .withProductId(new ProductId(OrderValidTestData.SecondProduct.ID))
                .withUnitPrice(new Money(new BigDecimal("15"), TRY))
                .build();
        final Order order = new OrderBuilder()
                .withValidValues()
                .withLines(List.of(headphones, cable))
                .build();

        // When
        final OrderResponse response = mapper.toOrderResponse(order);

        // Then
        assertThat(response.orderId()).isEqualTo(order.getId().value());
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.cancellationReason()).isNull();
        assertThat(response.placedAt()).isEqualTo(NOW);
        assertThat(response.lastModifiedAt()).isEqualTo(NOW);
        assertThat(response.lines()).containsExactly(
                new OrderLineResponse(OrderValidTestData.Product.ID, OrderValidTestData.Product.NAME,
                        new MoneyResponse("249.90", "TRY"), 2, new MoneyResponse("499.80", "TRY")),
                new OrderLineResponse(OrderValidTestData.SecondProduct.ID, OrderValidTestData.Product.NAME,
                        new MoneyResponse("15.00", "TRY"), 1, new MoneyResponse("15.00", "TRY")));
        assertThat(response.total()).isEqualTo(new MoneyResponse("514.80", "TRY"));
    }

    @Test
    @DisplayName("İptal edilmiş siparişin cevabında iptal sebebi yer alır")
    void givenCancelledOrder_whenToOrderResponse_thenCancellationReasonMapped() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withStatus(OrderStatus.CANCELLED)
                .withCancellationReason(CancellationReason.PAYMENT_DECLINED)
                .build();

        // When
        final OrderResponse response = mapper.toOrderResponse(order);

        // Then
        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(response.cancellationReason()).isEqualTo("PAYMENT_DECLINED");
    }
}
