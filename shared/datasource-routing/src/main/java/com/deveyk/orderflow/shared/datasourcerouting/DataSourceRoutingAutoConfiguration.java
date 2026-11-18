package com.deveyk.orderflow.shared.datasourcerouting;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;

@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnClass(HikariDataSource.class)
@ConditionalOnProperty(prefix = "orderflow.datasource", name = {"primary.jdbc-url", "replica.jdbc-url"})
public class DataSourceRoutingAutoConfiguration {

    public static final String PRIMARY_DATA_SOURCE = "primaryDataSource";
    public static final String REPLICA_DATA_SOURCE = "replicaDataSource";

    @Bean(PRIMARY_DATA_SOURCE)
    @ConfigurationProperties("orderflow.datasource.primary")
    public HikariDataSource primaryDataSource() {
        return new HikariDataSource();
    }

    @Bean(REPLICA_DATA_SOURCE)
    @ConfigurationProperties("orderflow.datasource.replica")
    public HikariDataSource replicaDataSource() {
        return new HikariDataSource();
    }

    // Bağlantı ilk SQL'e kadar alınmaz; o anda transaction read-only ise replica, değilse primary pool'dan gelir.
    // Transaction dışındaki erişim (Flyway dahil) primary'ye gider.
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier(PRIMARY_DATA_SOURCE) final DataSource primary,
                                 @Qualifier(REPLICA_DATA_SOURCE) final DataSource replica) {
        final LazyConnectionDataSourceProxy proxy = new LazyConnectionDataSourceProxy(primary);
        proxy.setReadOnlyDataSource(replica);
        return proxy;
    }
}
