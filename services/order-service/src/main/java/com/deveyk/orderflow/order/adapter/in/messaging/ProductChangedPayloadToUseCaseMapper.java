package com.deveyk.orderflow.order.adapter.in.messaging;

import com.deveyk.orderflow.order.application.product.usecase.ApplyProductChangeUseCase;
import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.product.ProductChangedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;

import java.math.BigDecimal;
import java.util.Currency;

final class ProductChangedPayloadToUseCaseMapper {

    private ProductChangedPayloadToUseCaseMapper() {
    }

    static ApplyProductChangeUseCase toUseCase(final ProductChangedPayload payload) {

        if (payload.productVersion() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.productVersion");
        }

        return new ApplyProductChangeUseCase(new ProductId(payload.productId()), payload.name(),
                toMoney(payload.unitPrice()), payload.productVersion());
    }

    private static Money toMoney(final MoneyAmount unitPrice) {

        if (unitPrice == null) {
            return null;
        }

        if (unitPrice.amount() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.unitPrice.amount");
        }

        if (unitPrice.currency() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.unitPrice.currency");
        }

        final BigDecimal amount;
        final Currency currency;
        try {

            amount = new BigDecimal(unitPrice.amount());
            currency = Currency.getInstance(unitPrice.currency());

        } catch (final IllegalArgumentException e) {   // NumberFormatException de bir IllegalArgumentException
            throw new InvalidEventException(MessagingErrorCode.INVALID_EVENT_FIELD, e, "payload.unitPrice", unitPrice);
        }

        return new Money(amount, currency);
    }
}
