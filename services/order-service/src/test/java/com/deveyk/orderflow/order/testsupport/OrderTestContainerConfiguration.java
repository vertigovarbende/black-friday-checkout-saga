package com.deveyk.orderflow.order.testsupport;

import com.deveyk.orderflow.shared.testsupport.LogTrackerConfiguration;
import com.deveyk.orderflow.shared.testsupport.container.OrderflowContainers;
import com.deveyk.orderflow.shared.testsupport.messaging.MessagePublisher;
import com.deveyk.orderflow.shared.testsupport.web.OrderflowMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTracing
@Import(OrderDatabaseCleaner.class)
public abstract class OrderTestContainerConfiguration extends LogTrackerConfiguration {

    protected static final PostgreSQLContainer POSTGRES = OrderflowContainers.postgres("order_service");
    protected static final KafkaContainer KAFKA = OrderflowContainers.kafka();

    static {
        Startables.deepStart(POSTGRES, KAFKA).join();
    }

    // Primary ve replica aynı container'a bağlanır; gerçek yönlendirme shared-datasource-routing'in IT'lerinde test edilir.
    @DynamicPropertySource
    static void overrideProperties(final DynamicPropertyRegistry registry) {

        for (final String pool : List.of("primary", "replica")) {
            registry.add("orderflow.datasource." + pool + ".jdbc-url", POSTGRES::getJdbcUrl);
            registry.add("orderflow.datasource." + pool + ".username", POSTGRES::getUsername);
            registry.add("orderflow.datasource." + pool + ".password", POSTGRES::getPassword);
        }

        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/testdata");
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("orderflow.kafka.topics.replication-factor", () -> "1");   // Tek broker: topic'ler RF 1 ile açılır.
        registry.add("orderflow.kafka.topics.min-insync-replicas", () -> "1");
        registry.add("management.tracing.export.otlp.enabled", () -> "false");
    }

    @Autowired
    private OrderDatabaseCleaner databaseCleaner;

    protected OrderflowMockMvc orderflowMockMvc;

    protected MessagePublisher messagePublisher;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    @BeforeEach
    void setUpOrderflowMockMvc(@Autowired final MockMvc mockMvc) {
        orderflowMockMvc = new OrderflowMockMvc(mockMvc);
    }

    @BeforeEach
    void setUpMessagePublisher(@Autowired final JsonMapper jsonMapper) {
        messagePublisher = MessagePublisher.on(KAFKA.getBootstrapServers(), jsonMapper);
    }

    @AfterEach
    void closeMessagePublisher() {
        messagePublisher.close();
    }
}
