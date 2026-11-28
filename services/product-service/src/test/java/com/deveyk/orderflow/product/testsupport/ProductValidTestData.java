package com.deveyk.orderflow.product.testsupport;

import com.deveyk.orderflow.product.domain.common.model.Money;
import com.deveyk.orderflow.product.domain.common.model.ProductId;
import com.deveyk.orderflow.product.domain.common.model.Quantity;
import com.deveyk.orderflow.product.domain.reservation.model.OrderId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

public final class ProductValidTestData {

    public static final Instant NOW = Instant.parse("2026-11-28T10:00:00Z");
    public static final Instant LATER = Instant.parse("2026-11-28T10:05:00Z");
    public static final Currency TRY = Currency.getInstance("TRY");

    private ProductValidTestData() {
    }

    // order-service'in OrderValidTestData.Product'ı ile aynı ürün: iki servisin testleri aynı veriden konuşur.
    public static final class Product {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000101");
        public static final ProductId PRODUCT_ID = new ProductId(ID);
        public static final String NAME = "Wireless Headphones";
        public static final BigDecimal UNIT_PRICE = new BigDecimal("249.90");
        public static final Money UNIT_PRICE_MONEY = new Money(UNIT_PRICE, TRY);
        public static final Quantity ON_HAND = new Quantity(10);

        private Product() {
        }
    }

    public static final class SecondProduct {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000102");
        public static final ProductId PRODUCT_ID = new ProductId(ID);
        public static final String NAME = "USB-C Cable";
        public static final BigDecimal UNIT_PRICE = new BigDecimal("15.00");
        public static final Money UNIT_PRICE_MONEY = new Money(UNIT_PRICE, TRY);
        public static final Quantity ON_HAND = new Quantity(50);

        private SecondProduct() {
        }
    }

    public static final class Order {

        public static final UUID ID = UUID.fromString("0198a1b2-0000-7000-8000-000000000201");
        public static final OrderId ORDER_ID = new OrderId(ID);
        public static final Quantity QUANTITY = new Quantity(2);

        private Order() {
        }
    }
}
