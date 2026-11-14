package com.deveyk.orderflow.shared.domain.testsupport;

import com.deveyk.orderflow.shared.domain.Entity;

public final class OtherSampleEntity extends Entity<String> {

    private final String id;

    private OtherSampleEntity(final String id) {
        this.id = id;
    }

    public static OtherSampleEntity of(final String id) {
        return new OtherSampleEntity(id);
    }

    @Override
    public String getId() {
        return id;
    }

}
