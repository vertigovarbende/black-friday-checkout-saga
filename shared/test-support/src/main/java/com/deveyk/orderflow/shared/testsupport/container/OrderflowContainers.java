package com.deveyk.orderflow.shared.testsupport.container;

import org.testcontainers.postgresql.PostgreSQLContainer;

public final class OrderflowContainers {

    // infra/docker/compose.yml ile aynı image
    public static final String POSTGRES_IMAGE = "postgres:18.7";

    private OrderflowContainers() {
    }

    // wal_level=logical: teslim garantisi testlerinde aynı container'a Debezium bağlanabilsin
    public static PostgreSQLContainer postgres(final String databaseName) {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName(databaseName)
                .withCommand("postgres", "-c", "wal_level=logical");
    }
}
