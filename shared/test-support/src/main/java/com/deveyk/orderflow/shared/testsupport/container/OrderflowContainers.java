package com.deveyk.orderflow.shared.testsupport.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public final class OrderflowContainers {

    public static final String POSTGRES_IMAGE = "postgres:18.7";
    public static final String KAFKA_IMAGE = "apache/kafka:4.2.2";
    public static final String CONNECT_IMAGE = "quay.io/debezium/connect:3.6.3.Final";

    public static final Network NETWORK = Network.newNetwork();
    public static final String POSTGRES_ALIAS = "postgres";
    public static final int POSTGRES_INTERNAL_PORT = 5432;

    private static final String KAFKA_INTERNAL_BOOTSTRAP = "kafka:19092";
    private static final int CONNECT_PORT = 8083;

    private OrderflowContainers() {
    }

    // wal_level=logical: teslim garantisi testlerinde aynı container'a Debezium bağlanabilsin
    public static PostgreSQLContainer postgres(final String databaseName) {
        return new PostgreSQLContainer(POSTGRES_IMAGE)
                .withDatabaseName(databaseName)
                .withNetwork(NETWORK)
                .withNetworkAliases(POSTGRES_ALIAS)
                .withCommand("postgres", "-c", "wal_level=logical");
    }

    // Tek broker. Otomatik topic oluşturma compose'daki gibi kapalı: topic'leri servisin NewTopic'leri açar.
    public static KafkaContainer kafka() {
        return new KafkaContainer(KAFKA_IMAGE)
                .withNetwork(NETWORK)
                .withListener(KAFKA_INTERNAL_BOOTSTRAP)
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false");
    }

    // Sadece teslim garantisi testlerinde (07 §4.5). Connect'in kendi topic'leri tek broker için RF 1.
    public static GenericContainer<?> debeziumConnect(final KafkaContainer kafka) {
        return new GenericContainer<>(DockerImageName.parse(CONNECT_IMAGE))
                .withNetwork(NETWORK)
                .dependsOn(kafka)
                .withExposedPorts(CONNECT_PORT)
                .withEnv("BOOTSTRAP_SERVERS", KAFKA_INTERNAL_BOOTSTRAP)
                .withEnv("GROUP_ID", "orderflow-connect")
                .withEnv("CONFIG_STORAGE_TOPIC", "_connect_configs")
                .withEnv("OFFSET_STORAGE_TOPIC", "_connect_offsets")
                .withEnv("STATUS_STORAGE_TOPIC", "_connect_status")
                .withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("HEAP_OPTS", "-Xms256m -Xmx512m")
                .waitingFor(Wait.forHttp("/connectors").forPort(CONNECT_PORT).forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(2)));
    }

    public static String connectUrl(final GenericContainer<?> connect) {
        return "http://" + connect.getHost() + ":" + connect.getMappedPort(CONNECT_PORT);
    }
}
