package com.deveyk.orderflow.order.testsupport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public final class OrderValidTestData {

    public static final Instant NOW = Instant.parse("2026-11-27T10:00:00Z");
    public static final Currency TRY = Currency.getInstance("TRY");
    public static final Currency EUR = Currency.getInstance("EUR");

    private OrderValidTestData() {
    }

    public static final class Customer {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000001");

        private Customer() {
        }
    }

    public static final class Product {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000101");
        public static final String NAME = "Wireless Headphones";
        public static final BigDecimal UNIT_PRICE = new BigDecimal("249.90");

        private Product() {
        }
    }
}
