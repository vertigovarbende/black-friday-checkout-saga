package com.deveyk.orderflow.product.domain.reservation.model;

import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockRejectionTest {

    @Test
    @DisplayName("Satırdan gelen ret sebebi ürünüyle birlikte; kapanmış rezervasyon cevabı ürünsüz")
    void givenReasons_whenCreate_thenProductOnlyForLineReasons() {
        // When / Then
        assertThat(new StockRejection(RejectionReason.INSUFFICIENT_STOCK, ProductValidTestData.Product.PRODUCT_ID).productId())
                .isEqualTo(ProductValidTestData.Product.PRODUCT_ID);
        assertThat(StockRejection.reservationClosed().productId()).isNull();
    }

    @Test
    @DisplayName("Satır sebebinde ürün zorunlu, RESERVATION_CLOSED'da ürün olamaz")
    void givenMismatchedProduct_whenCreate_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> new StockRejection(RejectionReason.PRICE_CHANGED, null))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.REJECTED_PRODUCT_MISMATCH));
        assertThatThrownBy(() -> new StockRejection(RejectionReason.RESERVATION_CLOSED, ProductValidTestData.Product.PRODUCT_ID))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(StockReservationErrorCode.REJECTED_PRODUCT_MISMATCH));
    }
}
