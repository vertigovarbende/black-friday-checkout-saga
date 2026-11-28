package com.deveyk.orderflow.product.domain.common.model;

import com.deveyk.orderflow.product.domain.common.exception.CommonDomainErrorCode;
import com.deveyk.orderflow.shared.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

// order-service'teki Money'nin karşılığı: iş kavramları shared'e girmez, her servis kendisininkini yazar (01 §3.3 kural 1).
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

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
