package com.deveyk.orderflow.shared.datasourcerouting.testsupport;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Map;

public class NaiveReadOnlyRoutingDataSource extends AbstractRoutingDataSource {

    private static final String PRIMARY = "primary";
    private static final String REPLICA = "replica";

    public NaiveReadOnlyRoutingDataSource(final DataSource primary, final DataSource replica) {
        setTargetDataSources(Map.of(PRIMARY, primary, REPLICA, replica));
        setDefaultTargetDataSource(primary);
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? REPLICA : PRIMARY;
    }
}
