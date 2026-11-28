package com.deveyk.orderflow.order.testsupport;

import com.deveyk.orderflow.shared.testsupport.container.DebeziumConnectors;
import com.deveyk.orderflow.shared.testsupport.container.OrderflowContainers;
import com.deveyk.orderflow.shared.testsupport.messaging.MessageProbe;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;

import java.nio.file.Path;
import java.util.Map;

// Teslim garantisi testleri (07 §4.5): + Debezium Connect. Connect ağır olduğu için sadece bu tabandan türeyen testler açar.
public abstract class OrderDeliveryTestConfiguration extends OrderTestContainerConfiguration {

    // Testler modül klasöründen (services/order-service) koşar.
    private static final Path CONNECTOR_CONFIG = Path.of("../../infra/debezium/order-outbox.json");
    private static final GenericContainer<?> CONNECT = OrderflowContainers.debeziumConnect(KAFKA);

    private static boolean connectorRegistered;

    static {
        CONNECT.start();
    }

    // Publication'ı Flyway (V4) açar ve connector onu kendisi açmaz (autocreate disabled); bu yüzden kayıt
    // Spring context açıldıktan sonra, ilk testten önce bir kez yapılır.
    @BeforeEach
    void registerConnector() {
        if (connectorRegistered) {
            return;
        }
        DebeziumConnectors.register(CONNECT, CONNECTOR_CONFIG, Map.of(
                "database.hostname", OrderflowContainers.POSTGRES_ALIAS,
                "database.port", String.valueOf(OrderflowContainers.POSTGRES_INTERNAL_PORT),
                "database.user", POSTGRES.getUsername(),
                "database.password", POSTGRES.getPassword()));
        connectorRegistered = true;
    }

    protected MessageProbe messageProbe(final String topic) {
        return MessageProbe.on(KAFKA.getBootstrapServers(), topic);
    }
}
