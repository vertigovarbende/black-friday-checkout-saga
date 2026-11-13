package com.deveyk.orderflow.shared.domain.testsupport;

import com.deveyk.orderflow.shared.domain.Entity;

public final class SampleEntity extends Entity<String> {

    private final String id;
    private final String name;

    private SampleEntity(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static SampleEntity of(final String id, final String name) {
        return new SampleEntity(id, name);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
