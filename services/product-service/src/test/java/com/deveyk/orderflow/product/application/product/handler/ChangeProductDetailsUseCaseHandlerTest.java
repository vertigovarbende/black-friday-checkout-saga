package com.deveyk.orderflow.product.application.product.handler;

import com.deveyk.orderflow.product.application.product.port.ProductDomainEventPort;
import com.deveyk.orderflow.product.application.product.port.ProductReadPort;
import com.deveyk.orderflow.product.application.product.port.ProductSavePort;
import com.deveyk.orderflow.product.application.product.usecase.ChangeProductDetailsUseCase;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.product.exception.ProductNotFoundException;
import com.deveyk.orderflow.product.domain.product.model.Product;
import com.deveyk.orderflow.product.testsupport.ProductValidTestData;
import com.deveyk.orderflow.product.testsupport.builder.ProductBuilder;
import com.deveyk.orderflow.shared.testsupport.OrderflowUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static com.deveyk.orderflow.product.testsupport.ProductValidTestData.TRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChangeProductDetailsUseCaseHandlerTest extends OrderflowUnitTest {

    private static final ProductId PRODUCT_ID = ProductValidTestData.Product.PRODUCT_ID;
    private static final Money NEW_PRICE = new Money(new BigDecimal("279.90"), TRY);

    @Mock
    private ProductReadPort productReadPort;

    @Mock
    private ProductSavePort productSavePort;

    @Mock
    private ProductDomainEventPort productDomainEventPort;

    private ChangeProductDetailsUseCaseHandler handler;

    @BeforeEach
    void setUpHandler() {
        handler = new ChangeProductDetailsUseCaseHandler(productReadPort, productSavePort, productDomainEventPort);
    }

    @Test
    @DisplayName("Sadece fiyat verilirse ad korunur; ürün kaydedilir ve product.changed yayınlanır")
    void givenNewPriceOnly_whenHandle_thenPriceChangedNameKeptAndEventPublished() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();
        Mockito.when(productReadPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // When
        final Product result = handler.handle(new ChangeProductDetailsUseCase(PRODUCT_ID, null, NEW_PRICE));

        // Then
        assertThat(result.getName()).isEqualTo(ProductValidTestData.Product.NAME);
        assertThat(result.getUnitPrice()).isEqualTo(NEW_PRICE);
        assertThat(result.getCatalogVersion()).isEqualTo(2L);
        Mockito.verify(productSavePort).update(product);
        Mockito.verify(productDomainEventPort).publishProductChanged(product);
    }

    @Test
    @DisplayName("Ad ve fiyat aynıysa kaydedilmez, product.changed yayınlanmaz")
    void givenSameDetails_whenHandle_thenNothingSavedOrPublished() {
        // Given
        final Product product = new ProductBuilder().withValidValues().build();
        Mockito.when(productReadPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        // When
        handler.handle(new ChangeProductDetailsUseCase(PRODUCT_ID, ProductValidTestData.Product.NAME, ProductValidTestData.Product.UNIT_PRICE_MONEY));

        // Then
        Mockito.verifyNoInteractions(productSavePort, productDomainEventPort);
    }

    @Test
    @DisplayName("Ürün yoksa ProductNotFoundException (404)")
    void givenUnknownProduct_whenHandle_thenNotFound() {
        // Given
        Mockito.when(productReadPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> handler.handle(new ChangeProductDetailsUseCase(PRODUCT_ID, null, NEW_PRICE)))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
