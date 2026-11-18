package com.deveyk.orderflow.shared.datasourcerouting;

import com.deveyk.orderflow.shared.datasourcerouting.testsupport.DataSourceRoutingTestContainerConfiguration;
import com.deveyk.orderflow.shared.datasourcerouting.testsupport.NaiveReadOnlyRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingWithoutLazyProxyIT extends DataSourceRoutingTestContainerConfiguration {

    @Autowired
    @Qualifier(DataSourceRoutingAutoConfiguration.PRIMARY_DATA_SOURCE)
    private HikariDataSource primaryPool;

    @Autowired
    @Qualifier(DataSourceRoutingAutoConfiguration.REPLICA_DATA_SOURCE)
    private HikariDataSource replicaPool;

    @Test
    @DisplayName("Proxy'siz yönlendirici read-only transaction'ı sessizce primary'ye gönderir")
    void givenRoutingWithoutLazyProxy_whenReadOnlyRead_thenWronglyPrimary() {
        // Given
        final DataSource routing = new NaiveReadOnlyRoutingDataSource(primaryPool, replicaPool);

        // When
        final String marker = readInReadOnlyTransaction(routing);

        // Then
        assertThat(marker).isEqualTo("primary");
    }

    @Test
    @DisplayName("Aynı yönlendirici LazyConnectionDataSourceProxy arkasında read-only transaction'ı replica'ya gönderir")
    void givenRoutingBehindLazyProxy_whenReadOnlyRead_thenReplica() {
        // Given
        final DataSource routing = new LazyConnectionDataSourceProxy(
                new NaiveReadOnlyRoutingDataSource(primaryPool, replicaPool));

        // When
        final String marker = readInReadOnlyTransaction(routing);

        // Then
        assertThat(marker).isEqualTo("replica");
    }

    private static String readInReadOnlyTransaction(final DataSource dataSource) {
        final TransactionTemplate readOnly = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        readOnly.setReadOnly(true);
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return readOnly.execute(status -> jdbcTemplate.queryForObject("SELECT name FROM marker", String.class));
    }
}
