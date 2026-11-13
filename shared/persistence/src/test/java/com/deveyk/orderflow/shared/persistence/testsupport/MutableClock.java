package com.deveyk.orderflow.shared.persistence.testsupport;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class MutableClock extends Clock {

    private Instant now;

    private MutableClock(final Instant now) {
        this.now = now;
    }

    public static MutableClock at(final Instant now) {
        return new MutableClock(now);
    }

    public void setTo(final Instant instant) {
        this.now = instant;
    }

    public void advance(final Duration duration) {
        this.now = this.now.plus(duration);
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(final ZoneId zone) {
        throw new UnsupportedOperationException("MutableClock always runs in UTC");
    }
}
