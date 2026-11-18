package com.deveyk.orderflow.shared.datasourcerouting.testsupport;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkerReader {

    private static final String SELECT_MARKER = "SELECT name FROM marker";

    private final JdbcTemplate jdbcTemplate;

    public MarkerReader(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public String readInReadOnlyTransaction() {
        return jdbcTemplate.queryForObject(SELECT_MARKER, String.class);
    }

    @Transactional
    public String readInReadWriteTransaction() {
        return jdbcTemplate.queryForObject(SELECT_MARKER, String.class);
    }

    public String readWithoutTransaction() {
        return jdbcTemplate.queryForObject(SELECT_MARKER, String.class);
    }

    @Transactional(readOnly = true)
    public void writeInReadOnlyTransaction() {
        jdbcTemplate.update("INSERT INTO marker (name) VALUES ('written')");
    }
}
