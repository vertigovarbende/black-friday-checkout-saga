package com.deveyk.orderflow.order.testsupport;

import com.deveyk.orderflow.shared.testsupport.LogTrackerConfiguration;
import com.deveyk.orderflow.shared.testsupport.container.OrderflowContainers;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

@SpringBootTest
@Import(OrderDatabaseCleaner.class)
public abstract class OrderTestContainerConfiguration extends LogTrackerConfiguration {

    private static final PostgreSQLContainer POSTGRES = OrderflowContainers.postgres("order_service");

    static {
        POSTGRES.start();
    }

    // Primary ve replica aynı container'a bağlanır; gerçek yönlendirme shared-datasource-routing'in IT'lerinde test edilir.
    @DynamicPropertySource
    static void overrideProperties(final DynamicPropertyRegistry registry) {
        for (final String pool : List.of("primary", "replica")) {
            registry.add("orderflow.datasource." + pool + ".jdbc-url", POSTGRES::getJdbcUrl);
            registry.add("orderflow.datasource." + pool + ".username", POSTGRES::getUsername);
            registry.add("orderflow.datasource." + pool + ".password", POSTGRES::getPassword);
        }
    }

    @Autowired
    private OrderDatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }
}
