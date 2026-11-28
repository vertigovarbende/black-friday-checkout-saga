package com.deveyk.orderflow.shared.messaging.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// Not: shared modüller shared-test-support'u kullanamadığı için (01-paket-yapisi-kararlari §3.3, kural 5)
// ErrorCodeAssert yerine kontroller burada elle yazılır.
class MessagingErrorCodeTest {

    @Test
    @DisplayName("Bütün kodlar MSG_<3 hane> formatındadır ve tekildir")
    void givenMessagingErrorCodes_whenCheckCodes_thenFormatAndUniqueness() {
        // Given
        final List<String> codes = Arrays.stream(MessagingErrorCode.values()).map(MessagingErrorCode::code).toList();

        // Then
        assertThat(codes).allMatch(code -> code.matches("^MSG_\\d{3}$"));
        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Her kodun başlığı dolu ve mesajı geçerli bir MessageFormat şablonudur")
    void givenMessagingErrorCodes_whenCheckHeaderAndMessage_thenValid() {
        for (final MessagingErrorCode errorCode : MessagingErrorCode.values()) {
            // Then
            assertThat(errorCode.header()).isNotBlank();
            assertThatCode(() -> new MessageFormat(errorCode.defaultMessage())).doesNotThrowAnyException();
        }
    }
}
