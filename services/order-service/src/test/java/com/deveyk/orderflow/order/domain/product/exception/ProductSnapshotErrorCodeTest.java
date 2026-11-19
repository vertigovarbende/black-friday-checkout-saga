package com.deveyk.orderflow.order.domain.product.exception;

import com.deveyk.orderflow.shared.testsupport.exception.ErrorCodeAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductSnapshotErrorCodeTest {

    @Test
    @DisplayName("ProductSnapshotErrorCode kodları ORD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenProductSnapshotErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(ProductSnapshotErrorCode.class, "ORD");
    }
}
