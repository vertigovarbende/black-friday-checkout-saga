package com.deveyk.orderflow.shared.messaging.contract;

import java.math.BigDecimal;
import java.util.Currency;

public record MoneyAmount(String amount, String currency) {

    public static MoneyAmount of(final BigDecimal amount, final Currency currency) {
        return new MoneyAmount(amount.toPlainString(), currency.getCurrencyCode());
    }
}
