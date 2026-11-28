package com.deveyk.orderflow.product.adapter.in.web.mapper;

import com.deveyk.orderflow.product.adapter.in.web.request.ChangeProductDetailsRequest;
import com.deveyk.orderflow.product.adapter.in.web.request.MoneyRequest;
import com.deveyk.orderflow.product.application.product.usecase.ChangeProductDetailsUseCase;
import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRequestToUseCaseMapperTest {

    @Test
    @DisplayName("Verilmeyen alanlar use case'te null kalır (PATCH: değişmez)")
    void givenOnlyPrice_whenToUseCase_thenNameNullAndPriceMapped() {
        // When
        final ChangeProductDetailsUseCase useCase = ProductRequestToUseCaseMapper.toChangeProductDetailsUseCase(
                ProductValidTestData.Product.ID, new ChangeProductDetailsRequest(null, new MoneyRequest(new BigDecimal("279.90"), "TRY")));

        // Then
        assertThat(useCase.productId()).isEqualTo(ProductValidTestData.Product.PRODUCT_ID);
        assertThat(useCase.name()).isNull();
        assertThat(useCase.unitPrice()).isEqualTo(new Money(new BigDecimal("279.90"), TRY));
    }

    @Test
    @DisplayName("Tanınmayan para birimi 400 PRD_106 olur (500 değil)")
    void givenUnknownCurrency_whenToUseCase_thenCurrencyUnknown() {
        // When / Then
        assertThatThrownBy(() -> ProductRequestToUseCaseMapper.toChangeProductDetailsUseCase(ProductValidTestData.Product.ID,
                new ChangeProductDetailsRequest(null, new MoneyRequest(BigDecimal.TEN, "XYZ"))))
                .isInstanceOfSatisfying(DomainValidationException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(CommonDomainErrorCode.CURRENCY_UNKNOWN));
    }
}
