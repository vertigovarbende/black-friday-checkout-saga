package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.testsupport.builder.ApplyProductChangeUseCaseBuilder;
import com.deveyk.orderflow.order.testsupport.builder.ProductChangedEventBuilder;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import com.deveyk.orderflow.shared.domain.exception.SharedErrorCode;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static com.deveyk.orderflow.order.testsupport.OrderValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductChangedPayloadToUseCaseMapperTest {

    @Test
    @DisplayName("Payload use case'e çevrilir: fiyat Money'ye, sürüm long'a")
    void givenValidPayload_whenToUseCase_thenMapped() {
        // Given
        final UUID productId = UUID.randomUUID();
        final ProductChangedPayload payload = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(productId)
                .withProductVersion(4L)
                .build()
                .payload();

        // When
        final ApplyProductChangeUseCase useCase = ProductChangedPayloadToUseCaseMapper.toUseCase(payload);

        // Then
        assertThat(useCase).isEqualTo(new ApplyProductChangeUseCaseBuilder()
                .withValidValues()
                .withProductId(new ProductId(productId))
                .withProductVersion(4L)
                .build());
    }

    @Test
    @DisplayName("Ürün sürümü yoksa EVENT_FIELD_MISSING: eski kopyayı ayırt etmenin yolu yok")
    void givenNoProductVersion_whenToUseCase_thenEventFieldMissing() {
        // Given
        final ProductChangedPayload payload = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductVersion(null)
                .build()
                .payload();

        // When / Then
        assertThatThrownBy(() -> ProductChangedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.EVENT_FIELD_MISSING));
    }

    @ParameterizedTest(name = "amount={0}, currency={1}")
    @CsvSource({"abc, TRY", "249.90, XYZ", "249.90, try"})
    @DisplayName("Sayı olmayan tutar ya da ISO olmayan para birimi INVALID_EVENT_FIELD ile reddedilir")
    void givenUnparseableUnitPrice_whenToUseCase_thenInvalidEventField(final String amount, final String currency) {
        // Given
        final ProductChangedPayload payload = new ProductChangedEventBuilder()
                .withValidValues()
                .withUnitPrice(new MoneyAmount(amount, currency))
                .build()
                .payload();

        // When / Then
        assertThatThrownBy(() -> ProductChangedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(InvalidEventException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MessagingErrorCode.INVALID_EVENT_FIELD));
    }

    @Test
    @DisplayName("Ürün kimliği yoksa domain'in kimlik kuralı reddeder (SHR_001)")
    void givenNoProductId_whenToUseCase_thenIdRequired() {
        // Given
        final ProductChangedPayload payload = new ProductChangedEventBuilder()
                .withValidValues()
                .withProductId(null)
                .build()
                .payload();

        // When / Then
        assertThatThrownBy(() -> ProductChangedPayloadToUseCaseMapper.toUseCase(payload))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(SharedErrorCode.ID_REQUIRED));
    }
}
