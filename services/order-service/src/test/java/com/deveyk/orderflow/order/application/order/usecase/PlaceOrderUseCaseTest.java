package com.deveyk.orderflow.order.application.order.usecase;

import com.deveyk.orderflow.order.domain.common.ProductId;
import com.deveyk.orderflow.order.domain.common.Quantity;
import com.deveyk.orderflow.order.domain.order.exception.OrderErrorCode;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;
import com.deveyk.orderflow.order.testsupport.OrderValidTestData;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceOrderUseCaseTest {

    private static final CustomerId CUSTOMER = new CustomerId(OrderValidTestData.Customer.ID);
    private static final PlaceOrderUseCase.Line LINE =
            new PlaceOrderUseCase.Line(new ProductId(OrderValidTestData.Product.ID), new Quantity(1));

    @Test
    @DisplayName("Müşterisi olmayan sipariş isteği oluşturulamaz")
    void givenNoCustomer_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new PlaceOrderUseCase(null, List.of(LINE)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.CUSTOMER_REQUIRED));
    }

    @Test
    @DisplayName("Satırı olmayan sipariş isteği oluşturulamaz")
    void givenNoLines_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new PlaceOrderUseCase(CUSTOMER, List.of()))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_LINES_REQUIRED));
    }

    @Test
    @DisplayName("Null satır içeren sipariş isteği oluşturulamaz")
    void givenNullLine_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new PlaceOrderUseCase(CUSTOMER, Arrays.asList(LINE, null)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.ORDER_LINES_REQUIRED));
    }

    @Test
    @DisplayName("Ürünü ya da adedi olmayan satır oluşturulamaz")
    void givenLineWithoutProductOrQuantity_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new PlaceOrderUseCase.Line(null, new Quantity(1)))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.PRODUCT_REQUIRED));

        assertThatThrownBy(() -> new PlaceOrderUseCase.Line(new ProductId(OrderValidTestData.Product.ID), null))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(OrderErrorCode.QUANTITY_REQUIRED));
    }

    @Test
    @DisplayName("Satır listesi kopyalanır; dışarıdaki liste değişse de istek değişmez")
    void givenMutableList_whenCreateAndModifyOriginal_thenUseCaseUnchanged() {
        // Given
        final List<PlaceOrderUseCase.Line> lines = new ArrayList<>(List.of(LINE));

        // When
        final PlaceOrderUseCase useCase = new PlaceOrderUseCase(CUSTOMER, lines);
        lines.clear();

        // Then
        assertThat(useCase.lines()).containsExactly(LINE);
    }
}
