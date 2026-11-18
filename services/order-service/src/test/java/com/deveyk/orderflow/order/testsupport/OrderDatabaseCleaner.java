package com.deveyk.orderflow.order.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;

public class OrderDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public OrderDatabaseCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE order_lines, orders CASCADE");
    }
}
