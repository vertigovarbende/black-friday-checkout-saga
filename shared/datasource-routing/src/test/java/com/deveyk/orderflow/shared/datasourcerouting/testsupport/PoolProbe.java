package com.deveyk.orderflow.shared.datasourcerouting.testsupport;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public final class PoolProbe {

    private PoolProbe() {
    }

    // Pool ilk bağlantı istenene kadar başlamaz; başlamamış pool'da ödünç verilmiş bağlantı da yoktur.
    public static int activeConnections(final HikariDataSource dataSource) {
        final HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        return pool == null ? 0 : pool.getActiveConnections();
    }
}
