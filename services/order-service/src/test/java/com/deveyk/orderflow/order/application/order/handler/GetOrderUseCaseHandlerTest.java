package com.deveyk.orderflow.order.application.order.handler;

import com.deveyk.orderflow.order.application.order.usecase.GetOrderUseCase;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.exception.OrderNotFoundException;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.domain.order.model.Order;
import com.deveyk.orderflow.order.domain.order.model.OrderId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.order.testsupport.builder.OrderBuilder;
import com.deveyk.orderflow.order.testsupport.fake.InMemoryOrderAdapter;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class GetOrderUseCaseHandlerTest extends OrderflowUnitTest {

    private static final CustomerId OWNER = new CustomerId(OrderValidTestData.Customer.ID);
    private static final CustomerId SOMEONE_ELSE = new CustomerId(UUID.fromString("0198a1b2-0000-7000-8000-000000000002"));

    private final InMemoryOrderAdapter orderAdapter = new InMemoryOrderAdapter();

    private GetOrderUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new GetOrderUseCaseHandler(orderAdapter);
    }

    @Test
    @DisplayName("Müşteri kendi siparişini okur")
    void givenOwnOrder_whenHandle_thenReturned() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withCustomerId(OWNER)
                .build();

        orderAdapter.create(order);

        // When
        final Order found = handler.handle(new GetOrderUseCase(order.getId(), OWNER));

        // Then
        assertThat(found).isEqualTo(order);
        assertThat(found.getCustomerId()).isEqualTo(OWNER);
    }

    @Test
    @DisplayName("Başka müşterinin siparişi yokmuş gibi davranılır: OrderNotFoundException")
    void givenSomeoneElsesOrder_whenHandle_thenNotFound() {
        // Given
        final Order order = new OrderBuilder()
                .withValidValues()
                .withCustomerId(OWNER)
                .build();

        orderAdapter.create(order);

        // When / Then
        assertThatThrownBy(() -> handler.handle(new GetOrderUseCase(order.getId(), SOMEONE_ELSE)))
                .isInstanceOfSatisfying(OrderNotFoundException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("Olmayan sipariş için OrderNotFoundException fırlar; mesajı başkasının siparişiyle aynıdır")
    void givenUnknownOrder_whenHandle_thenNotFoundWithSameMessage() {
        // Given
        final OrderId unknown = OrderId.newId();
        final Order someoneElses = new OrderBuilder()
                .withValidValues()
                .withCustomerId(SOMEONE_ELSE)
                .build();

        orderAdapter.create(someoneElses);

        // When
        final Throwable unknownError = catchThrowable(() -> handler.handle(new GetOrderUseCase(unknown, OWNER)));
        final Throwable foreignError = catchThrowable(() -> handler.handle(new GetOrderUseCase(someoneElses.getId(), OWNER)));

        // Then
        assertThat(unknownError).isInstanceOf(OrderNotFoundException.class);
        assertThat(foreignError).isInstanceOf(OrderNotFoundException.class);
        assertThat(unknownError.getMessage().replace(unknown.value().toString(), "<id>"))
                .isEqualTo(foreignError.getMessage().replace(someoneElses.getId().value().toString(), "<id>"));
    }
}
