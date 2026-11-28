package com.deveyk.orderflow.product.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

// Testler stoğu değiştirdiği için (reserve, release, commit, PATCH) ürünler her testten önce bilinen değerlerle yeniden
// yazılır: ProductValidTestData.Product (10 adet) ve .SecondProduct (50 adet). Lokal seed (db/seed) testlerde yüklenmez.
public class ProductDatabaseCleaner {

    private static final String INSERT_PRODUCT = """
            INSERT INTO products (id, name, unit_price, currency, on_hand, reserved, catalog_version, version, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 0, 1, 0, now(), now())
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProductDatabaseCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE stock_reservation_lines, stock_reservations, products, outbox_event, inbox_event CASCADE");
        jdbcTemplate.update(INSERT_PRODUCT, ProductValidTestData.Product.ID, ProductValidTestData.Product.NAME,
                ProductValidTestData.Product.UNIT_PRICE, ProductValidTestData.TRY.getCurrencyCode(), ProductValidTestData.Product.ON_HAND.value());
        jdbcTemplate.update(INSERT_PRODUCT, ProductValidTestData.SecondProduct.ID, ProductValidTestData.SecondProduct.NAME,
                ProductValidTestData.SecondProduct.UNIT_PRICE, ProductValidTestData.TRY.getCurrencyCode(), ProductValidTestData.SecondProduct.ON_HAND.value());
    }
}
