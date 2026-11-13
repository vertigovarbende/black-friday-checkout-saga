package com.deveyk.orderflow.shared.domain;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final UuidV7 SHARED = new UuidV7();

    private static final long MAX_TIMESTAMP = (1L << 48) - 1;
    private static final int MAX_SEQUENCE = 0xFFF;
    private static final int SEQUENCE_SEED_BOUND = 1 << 11;

    private long lastTimestamp = -1L;
    private int sequence;

    // Testler kendi örneklerini açar; böylece birbirlerinin zamanını etkilemezler.
    UuidV7() {

    }
    public static UUID next() {
        return SHARED.generate(System.currentTimeMillis());
    }

    public static long timestampOf(final UUID uuid) {
        return uuid.getMostSignificantBits() >>> 16;
    }

    UUID generate(final long epochMillis) {
        if (epochMillis < 0 || epochMillis > MAX_TIMESTAMP) {
            throw new IllegalArgumentException("Timestamp does not fit in 48 bits: " + epochMillis);
        }

        final long timestamp;
        final int counter;
        synchronized (this) {
            if (epochMillis > lastTimestamp) {
                lastTimestamp = epochMillis;
                sequence = RANDOM.nextInt(SEQUENCE_SEED_BOUND);
            } else if (sequence < MAX_SEQUENCE) {
                sequence++;
            } else {
                lastTimestamp++;
                sequence = 0;
            }
            timestamp = lastTimestamp;
            counter = sequence;
        }

        final long mostSignificantBits = (timestamp << 16) | 0x7000L | counter;
        final long leastSignificantBits = (RANDOM.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL) | 0x8000_0000_0000_0000L;
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
