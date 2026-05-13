/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * Affinity-internal bounds-check helpers. Mirrors the surface of
 * {@code net.openhft.chronicle.core.util.Bounds} but lives in this
 * leaf module so {@code Java-Thread-Affinity} (which Chronicle-Core
 * depends on) can validate sizes and offsets without a circular
 * dependency. Each helper requires a {@code message} naming the
 * value being checked.
 */
public final class Bounds {

    private Bounds() {
    }

    /**
     * Returns {@code value} after checking {@code value >= 0}.
     *
     * @throws IllegalArgumentException if {@code value} is negative
     */
    public static int requireSize(final int value, final String message) {
        if (value < 0)
            throw new IllegalArgumentException(message + " (size) must be non-negative: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking {@code value >= 0}.
     *
     * @throws IllegalArgumentException if {@code value} is negative
     */
    public static long requireSize(final long value, final String message) {
        if (value < 0L)
            throw new IllegalArgumentException(message + " (size) must be non-negative: " + value);
        return value;
    }

    /**
     * Returns {@code value} after checking {@code value >= 0}.
     *
     * @throws IllegalArgumentException if {@code value} is negative
     */
    public static long requireOffset(final long value, final String message) {
        if (value < 0L)
            throw new IllegalArgumentException(message + " (offset) must be non-negative: " + value);
        return value;
    }
}
