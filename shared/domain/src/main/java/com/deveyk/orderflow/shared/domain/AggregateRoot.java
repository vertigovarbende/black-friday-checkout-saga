package com.deveyk.orderflow.shared.domain;

public abstract class AggregateRoot<ID> extends Entity<ID> {

    private long version;

    protected AggregateRoot() {
    }

    public final long getVersion() {
        return version;
    }

    protected final void restoreVersion(final long version) {
        this.version = version;
    }
}
