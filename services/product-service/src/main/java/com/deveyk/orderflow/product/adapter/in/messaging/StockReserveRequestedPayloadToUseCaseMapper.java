package com.deveyk.orderflow.product.adapter.in.messaging;

import com.deveyk.orderflow.product.application.reservation.usecase.ReserveStockUseCase;
import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;
import com.deveyk.orderflow.product.domain.reservation.model.RequestedLine;
import com.deveyk.orderflow.shared.messaging.contract.MoneyAmount;
import com.deveyk.orderflow.shared.messaging.contract.stock.StockReserveRequestedPayload;
import com.deveyk.orderflow.shared.messaging.exception.InvalidEventException;
import com.deveyk.orderflow.shared.messaging.exception.MessagingErrorCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

// Mesajın yapısı burada (MSG_00x); satır kuralları (boş liste, adet, tekrar eden ürün) domain'de (PRD_2xx).
final class StockReserveRequestedPayloadToUseCaseMapper {

    private StockReserveRequestedPayloadToUseCaseMapper() {
    }

    static ReserveStockUseCase toUseCase(final StockReserveRequestedPayload payload) {

        if (payload.orderId() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.orderId");
        }

        if (payload.lines() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, "payload.lines");
        }

        final List<RequestedLine> lines = new ArrayList<>();
        for (int i = 0; i < payload.lines().size(); i++) {
            lines.add(toRequestedLine(payload.lines().get(i), "payload.lines[" + i + "]"));
        }

        return new ReserveStockUseCase(new OrderId(payload.orderId()), lines);
    }

    private static RequestedLine toRequestedLine(final StockReserveRequestedPayload.Line line, final String field) {

        if (line == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, field);
        }

        if (line.productId() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, field + ".productId");
        }

        return new RequestedLine(new ProductId(line.productId()), new Quantity(line.quantity()),
                toMoney(line.unitPrice(), field + ".unitPrice"));
    }

    private static Money toMoney(final MoneyAmount unitPrice, final String field) {

        if (unitPrice == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, field);
        }

        if (unitPrice.amount() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, field + ".amount");
        }

        if (unitPrice.currency() == null) {
            throw new InvalidEventException(MessagingErrorCode.EVENT_FIELD_MISSING, field + ".currency");
        }

        final BigDecimal amount;
        final Currency currency;
        try {

            amount = new BigDecimal(unitPrice.amount());
            currency = Currency.getInstance(unitPrice.currency());

        } catch (final IllegalArgumentException e) {   // NumberFormatException de bir IllegalArgumentException
            throw new InvalidEventException(MessagingErrorCode.INVALID_EVENT_FIELD, e, field, unitPrice);
        }

        return new Money(amount, currency);
    }
}
