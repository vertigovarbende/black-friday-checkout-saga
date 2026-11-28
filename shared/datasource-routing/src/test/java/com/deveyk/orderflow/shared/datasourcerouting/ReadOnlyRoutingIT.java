package com.deveyk.orderflow.shared.datasourcerouting;

import com.deveyk.orderflow.shared.datasourcerouting.testsupport.DataSourceRoutingTestContainerConfiguration;
import com.deveyk.orderflow.shared.datasourcerouting.testsupport.MarkerReader;
import com.deveyk.orderflow.shared.datasourcerouting.testsupport.PoolProbe;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadOnlyRoutingIT extends DataSourceRoutingTestContainerConfiguration {

    @Autowired
    private MarkerReader markerReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    @Qualifier(DataSourceRoutingAutoConfiguration.PRIMARY_DATA_SOURCE)
    private HikariDataSource primaryPool;

    @Autowired
    @Qualifier(DataSourceRoutingAutoConfiguration.REPLICA_DATA_SOURCE)
    private HikariDataSource replicaPool;

    @Test
    @DisplayName("Read-only transaction replica'dan okur")
    void givenReadOnlyTransaction_whenRead_thenReplica() {
        // When
        final String marker = markerReader.readInReadOnlyTransaction();

        // Then
        assertThat(marker).isEqualTo("replica");
    }

    @Test
    @DisplayName("Normal transaction primary'den okur")
    void givenReadWriteTransaction_whenRead_thenPrimary() {
        // When
        final String marker = markerReader.readInReadWriteTransaction();

        // Then
        assertThat(marker).isEqualTo("primary");
    }

    @Test
    @DisplayName("Transaction dışındaki erişim primary'ye gider")
    void givenNoTransaction_whenRead_thenPrimary() {
        // When
        final String marker = markerReader.readWithoutTransaction();

        // Then
        assertThat(marker).isEqualTo("primary");
    }

    @Test
    @DisplayName("Read-only transaction'da yazma replica bağlantısı tarafından reddedilir")
    void givenReadOnlyTransaction_whenWrite_thenRejected() {
        // When / Then
        assertThatThrownBy(() -> markerReader.writeInReadOnlyTransaction())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("read-only transaction");
    }

    @Test
    @DisplayName("Bağlantı transaction başında değil ilk SQL'de alınır")
    void givenReadOnlyTransaction_whenNoSqlYet_thenNoPhysicalConnection() {
        // Given
        final TransactionTemplate readOnly = new TransactionTemplate(transactionManager);
        readOnly.setReadOnly(true);

        readOnly.executeWithoutResult(status -> {
            // Then: transaction açık ama hiçbir pool'dan bağlantı ödünç alınmadı
            assertThat(PoolProbe.activeConnections(primaryPool)).isZero();
            assertThat(PoolProbe.activeConnections(replicaPool)).isZero();

            // When
            jdbcTemplate.queryForObject("SELECT name FROM marker", String.class);

            // Then: ilk SQL ile bağlantı replica'dan alındı ve transaction sonuna kadar tutuluyor
            assertThat(PoolProbe.activeConnections(replicaPool)).isEqualTo(1);
            assertThat(PoolProbe.activeConnections(primaryPool)).isZero();
        });
    }
}
