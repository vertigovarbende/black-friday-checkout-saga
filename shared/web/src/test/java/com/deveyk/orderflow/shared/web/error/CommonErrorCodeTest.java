package com.deveyk.orderflow.shared.web.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CommonErrorCodeTest {

    @Test
    @DisplayName("Bütün kodlar GEN_<3 hane> formatındadır ve tekildir")
    void givenCommonErrorCodes_whenCheckCodes_thenFormatAndUniqueness() {
        // Given
        final List<String> codes = Arrays.stream(CommonErrorCode.values()).map(CommonErrorCode::code).toList();

        // Then
        assertThat(codes).allMatch(code -> code.matches("^GEN_\\d{3}$"));
        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Her kodun başlığı dolu ve mesajı geçerli bir MessageFormat şablonudur")
    void givenCommonErrorCodes_whenCheckHeaderAndMessage_thenValid() {
        for (final CommonErrorCode errorCode : CommonErrorCode.values()) {
            // Then
            assertThat(errorCode.header()).isNotBlank();
            assertThatCode(() -> new MessageFormat(errorCode.defaultMessage())).doesNotThrowAnyException();
        }
    }
}
