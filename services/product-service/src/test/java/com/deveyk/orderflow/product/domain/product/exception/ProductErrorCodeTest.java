package com.deveyk.orderflow.product.domain.product.exception;

import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.product.domain.reservation.exception.StockReservationErrorCode;
import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import com.deveyk.orderflow.shared.testsupport.exception.ErrorCodeAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ProductErrorCodeTest {

    @Test
    @DisplayName("ProductErrorCode kodları PRD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenProductErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(ProductErrorCode.class, "PRD");
    }

    @Test
    @DisplayName("CommonDomainErrorCode kodları PRD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenCommonDomainErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(CommonDomainErrorCode.class, "PRD");
    }

    @Test
    @DisplayName("StockReservationErrorCode kodları PRD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenStockReservationErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(StockReservationErrorCode.class, "PRD");
    }

    @Test
    @DisplayName("Servisteki bütün error code enum'ları arasında kodlar tekildir")
    void givenAllProductServiceErrorCodes_whenCollectCodes_thenNoDuplicates() {
        // When
        final Stream<ErrorCode> all = Stream.<ErrorCode[]>of(ProductErrorCode.values(), CommonDomainErrorCode.values(),
                        StockReservationErrorCode.values())
                .flatMap(Arrays::stream);

        // Then
        assertThat(all.map(ErrorCode::code).toList()).doesNotHaveDuplicates();
    }
}
