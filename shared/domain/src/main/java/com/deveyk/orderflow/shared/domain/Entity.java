package com.deveyk.orderflow.shared.domain;

public abstract class Entity<ID> {

    protected Entity() {

    }

    public abstract ID getId();

    @Override
    public final boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        // getClass() güvenli: domain nesneleri JPA entity'si değil, Hibernate proxy'si yok
        return other != null
                && other.getClass() == getClass()
                && getId().equals(((Entity<?>) other).getId());
    }

    @Override
    public final int hashCode() {
        return getId().hashCode();
    }
}
