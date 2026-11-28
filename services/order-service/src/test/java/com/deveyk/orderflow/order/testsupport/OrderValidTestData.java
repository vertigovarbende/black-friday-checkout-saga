package com.deveyk.orderflow.order.testsupport;

import com.deveyk.orderflow.order.domain.common.model.Money;
import com.deveyk.orderflow.order.domain.common.model.ProductId;
import com.deveyk.orderflow.order.domain.order.model.CustomerId;

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
        public static final CustomerId CUSTOMER_ID = new CustomerId(ID);

        private Customer() {
        }
    }

    // Siparişi başkasına ait olan müşteri (sahiplik testleri)
    public static final class OtherCustomer {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000002");
        public static final CustomerId CUSTOMER_ID = new CustomerId(ID);

        private OtherCustomer() {
        }
    }

    public static final class Product {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000101");
        public static final ProductId PRODUCT_ID = new ProductId(ID);
        public static final String NAME = "Wireless Headphones";
        public static final BigDecimal UNIT_PRICE = new BigDecimal("249.90");
        public static final Money UNIT_PRICE_MONEY = new Money(UNIT_PRICE, TRY);

        private Product() {
        }
    }

    public static final class SecondProduct {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000102");
        public static final ProductId PRODUCT_ID = new ProductId(ID);
        public static final String NAME = "USB-C Cable";
        public static final BigDecimal UNIT_PRICE = new BigDecimal("15.00");
        public static final Money UNIT_PRICE_MONEY = new Money(UNIT_PRICE, TRY);

        private SecondProduct() {
        }
    }
}
