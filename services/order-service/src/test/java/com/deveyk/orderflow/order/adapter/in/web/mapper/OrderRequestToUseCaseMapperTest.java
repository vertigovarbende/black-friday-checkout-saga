package com.deveyk.orderflow.order.adapter.in.web.mapper;

import com.deveyk.orderflow.order.adapter.in.web.request.PlaceOrderRequest;
import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.application.order.usecase.PlaceOrderUseCase;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderRequestBuilder;
import com.deveyk.orderflow.order.testsupport.builder.PlaceOrderUseCaseBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRequestToUseCaseMapperTest {

    private static final UUID USER_ID = OrderValidTestData.Customer.ID;

    @Test
    @DisplayName("Sipariş isteği use case'e çevrilir: X-User-Id müşteri olur, satırların sırası korunur")
    void givenPlaceOrderRequest_whenToPlaceOrderUseCase_thenCustomerAndLinesMapped() {
        // Given
        final PlaceOrderRequest request = new PlaceOrderRequestBuilder()
                .withLine(OrderValidTestData.Product.ID, 2)
                .withLine(OrderValidTestData.SecondProduct.ID, 1)
                .build();

        // When
        final PlaceOrderUseCase useCase = OrderRequestToUseCaseMapper.toPlaceOrderUseCase(USER_ID, request);

        // Then
        assertThat(useCase).isEqualTo(new PlaceOrderUseCaseBuilder()
                .withCustomerId(OrderValidTestData.Customer.CUSTOMER_ID)
                .withLine(OrderValidTestData.Product.PRODUCT_ID, 2)
                .withLine(OrderValidTestData.SecondProduct.PRODUCT_ID, 1)
                .build());
    }

    @Test
    @DisplayName("Sipariş okuma isteği use case'e çevrilir")
    void givenUserAndOrderId_whenToGetOrderUseCase_thenMapped() {
        // Given
        final UUID orderId = OrderId.newId().value();

        // When
        final GetOrderUseCase useCase = OrderRequestToUseCaseMapper.toGetOrderUseCase(USER_ID, orderId);

        // Then
        assertThat(useCase).isEqualTo(new GetOrderUseCase(new OrderId(orderId), OrderValidTestData.Customer.CUSTOMER_ID));
    }
}
