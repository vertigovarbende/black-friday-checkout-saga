package com.deveyk.orderflow.order.domain.common;

import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {

        if (amount == null) {
            throw new DomainValidationException(CommonDomainErrorCode.AMOUNT_REQUIRED);
        }

        if (currency == null) {
            throw new DomainValidationException(CommonDomainErrorCode.CURRENCY_REQUIRED);
        }

        if (amount.signum() < 0) {
            throw new DomainValidationException(CommonDomainErrorCode.AMOUNT_NEGATIVE, amount.toPlainString());
        }

        final int fractionDigits = Math.max(currency.getDefaultFractionDigits(), 0);
        if (amount.stripTrailingZeros().scale() > fractionDigits) {
            throw new DomainValidationException(CommonDomainErrorCode.AMOUNT_SCALE_TOO_LARGE, currency.getCurrencyCode(), String.valueOf(fractionDigits), amount.toPlainString());
        }

        amount = amount.setScale(fractionDigits, RoundingMode.UNNECESSARY);
    }

    public Money add(final Money other) {
        if (!currency.equals(other.currency)) {
            throw new DomainValidationException(CommonDomainErrorCode.CURRENCY_MISMATCH, currency.getCurrencyCode(), other.currency.getCurrencyCode());
        }

        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(final Quantity quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity.value())), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
