package com.deveyk.orderflow.order.domain.order.exception;

import com.deveyk.orderflow.order.adapter.in.messaging.MessagingErrorCode;
import com.deveyk.orderflow.order.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.order.domain.product.exception.ProductSnapshotErrorCode;
import com.deveyk.orderflow.shared.domain.exception.ErrorCode;
import com.deveyk.orderflow.shared.testsupport.exception.ErrorCodeAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderErrorCodeTest {

    @Test
    @DisplayName("OrderErrorCode kodları ORD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenOrderErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(OrderErrorCode.class, "ORD");
    }

    @Test
    @DisplayName("CommonDomainErrorCode kodları ORD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenCommonDomainErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(CommonDomainErrorCode.class, "ORD");
    }

    @Test
    @DisplayName("MessagingErrorCode kodları ORD öneki, tekil kod ve geçerli mesaj kurallarına uyar")
    void givenMessagingErrorCodes_whenAssertValid_thenNoViolations() {
        ErrorCodeAssert.assertValid(MessagingErrorCode.class, "ORD");
    }

    @Test
    @DisplayName("Servisteki bütün error code enum'ları arasında kodlar tekildir")
    void givenAllOrderServiceErrorCodes_whenCollectCodes_thenNoDuplicates() {
        // When
        final Stream<ErrorCode> all = Stream.<ErrorCode[]>of(OrderErrorCode.values(), CommonDomainErrorCode.values(), ProductSnapshotErrorCode.values(),
                        MessagingErrorCode.values())
                .flatMap(Arrays::stream);

        // Then
        assertThat(all.map(ErrorCode::code).toList()).doesNotHaveDuplicates();
    }
}
