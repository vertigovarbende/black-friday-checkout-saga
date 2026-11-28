package com.deveyk.orderflow.shared.datasourcerouting.testsupport;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

// İki bağımsız Postgres, aralarında replikasyon yok. Her birinde kim olduğunu söyleyen bir işaret satırı var.
@SpringBootTest
public abstract class DataSourceRoutingTestContainerConfiguration {

    private static final PostgreSQLContainer PRIMARY = new PostgreSQLContainer("postgres:18.7")
            .withInitScript("routing/primary-marker.sql");
    private static final PostgreSQLContainer REPLICA = new PostgreSQLContainer("postgres:18.7")
            .withInitScript("routing/replica-marker.sql");

    static {
        PRIMARY.start();
        REPLICA.start();
    }

    @DynamicPropertySource
    static void overrideProperties(final DynamicPropertyRegistry registry) {
        registry.add("orderflow.datasource.primary.jdbc-url", PRIMARY::getJdbcUrl);
        registry.add("orderflow.datasource.primary.username", PRIMARY::getUsername);
        registry.add("orderflow.datasource.primary.password", PRIMARY::getPassword);
        registry.add("orderflow.datasource.replica.jdbc-url", REPLICA::getJdbcUrl);
        registry.add("orderflow.datasource.replica.username", REPLICA::getUsername);
        registry.add("orderflow.datasource.replica.password", REPLICA::getPassword);
    }
}
