package com.deveyk.orderflow.shared.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// Not: shared modüller shared-test-support'u kullanamadığı için (01-paket-yapisi-kararlari §3.3, kural 5)
// ErrorCodeAssert yerine kontroller burada elle yazılır.
class SharedErrorCodeTest {

    @Test
    @DisplayName("Bütün kodlar SHR_<3 hane> formatındadır ve tekildir")
    void givenSharedErrorCodes_whenCheckCodes_thenFormatAndUniqueness() {
        // Given
        final List<String> codes = Arrays.stream(SharedErrorCode.values()).map(SharedErrorCode::code).toList();

        // Then
        assertThat(codes).allMatch(code -> code.matches("^SHR_\\d{3}$"));
        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Her kodun başlığı dolu ve mesajı geçerli bir MessageFormat şablonudur")
    void givenSharedErrorCodes_whenCheckHeaderAndMessage_thenValid() {
        for (final SharedErrorCode errorCode : SharedErrorCode.values()) {
            // Then
            assertThat(errorCode.header()).isNotBlank();
            assertThatCode(() -> new MessageFormat(errorCode.defaultMessage())).doesNotThrowAnyException();
        }
    }
}
