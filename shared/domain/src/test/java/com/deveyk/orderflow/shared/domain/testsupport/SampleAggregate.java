package com.deveyk.orderflow.shared.domain.testsupport;

import com.deveyk.orderflow.shared.domain.AggregateRoot;


public final class SampleAggregate extends AggregateRoot<String> {

    private final String id;

    private SampleAggregate(final String id, final long version) {
        this.id = id;
        restoreVersion(version);
    }

    public static SampleAggregate create(final String id) {
        return new SampleAggregate(id, 0L);
    }

    public static SampleAggregate rehydrate(final String id, final long version) {
        return new SampleAggregate(id, version);
    }

    @Override
    public String getId() {
        return id;
    }
}
