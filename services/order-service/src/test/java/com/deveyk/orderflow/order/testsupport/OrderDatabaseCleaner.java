package com.deveyk.orderflow.order.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

public class OrderDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public OrderDatabaseCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        // product_snapshots temizlenmez: seed satırları R__ migration'ından gelir ve testler onları değiştirmez;
        // listener testleri her seferinde yeni bir productId kullanır.
        jdbcTemplate.execute("TRUNCATE TABLE order_lines, orders, outbox_event, inbox_event CASCADE");
    }
}
