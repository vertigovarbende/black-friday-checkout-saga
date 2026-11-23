package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.response.MoneyResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderLineResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.OrderResponse;
import com.deveyk.orderflow.order.adapter.in.web.response.PlaceOrderResponse;
import com.deveyk.orderflow.order.domain.common.model.Quantity;
import com.deveyk.orderflow.order.domain.order.model.CancellationReason;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderLine;
import com.deveyk.orderflow.order.domain.order.model.OrderStatus;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.builder.OrderLineBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.NOW;
import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;

class OrderToResponseMapperTest {

    @Test
    @DisplayName("Yeni sipariş cevabı id ve durumu taşır")
    void givenPlacedOrder_whenToPlaceOrderResponse_thenIdAndStatus() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .build();

        // When
        final PlaceOrderResponse response = OrderToResponseMapper.toPlaceOrderResponse(order);

        // Then
        assertThat(response).isEqualTo(PlaceOrderResponse.builder()
                .orderId(order.getId().value())
                .status("PENDING")
                .build());
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
                .withSecondProduct()
                .build();
        final Order order = new OrderBuilder()
                .withValidValues()
                .withLines(List.of(headphones, cable))
                .build();

        // When
        final OrderResponse response = OrderToResponseMapper.toOrderResponse(order);

        // Then
        assertThat(response.orderId()).isEqualTo(order.getId().value());
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.cancellationReason()).isNull();
        assertThat(response.placedAt()).isEqualTo(NOW);
        assertThat(response.lastModifiedAt()).isEqualTo(NOW);
        assertThat(response.lines()).containsExactly(
                OrderLineResponse.builder()
                        .productId(OrderValidTestData.Product.ID)
                        .productName(OrderValidTestData.Product.NAME)
                        .unitPrice(money("249.90"))
                        .quantity(2)
                        .lineTotal(money("499.80"))
                        .build(),
                OrderLineResponse.builder()
                        .productId(OrderValidTestData.SecondProduct.ID)
                        .productName(OrderValidTestData.SecondProduct.NAME)
                        .unitPrice(money("15.00"))
                        .quantity(1)
                        .lineTotal(money("15.00"))
                        .build());
        assertThat(response.total()).isEqualTo(money("514.80"));
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
        final OrderResponse response = OrderToResponseMapper.toOrderResponse(order);

        // Then
        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(response.cancellationReason()).isEqualTo("PAYMENT_DECLINED");
    }

    private static MoneyResponse money(final String amount) {
        return MoneyResponse.builder()
                .amount(amount)
                .currency("TRY")
                .build();
    }
}
