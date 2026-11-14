package com.deveyk.orderflow.shared.web.testsupport;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

/**
 * Bean Validation testi için istek gövdesi. Annotation mesajları düz İngilizce metindir (06-exception-kararlari §4).
 */
public record ProbeBody(

        @NotNull(message = "Product id is required")
        UUID productId,
        @Positive(message = "Quantity must be at least 1")
        int quantity
) {

}
