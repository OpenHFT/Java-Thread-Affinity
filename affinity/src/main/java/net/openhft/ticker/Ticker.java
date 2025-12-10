/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.ticker;

import net.openhft.ticker.impl.JNIClock;
import net.openhft.ticker.impl.SystemClock;

/**
 * Static factory for available {@link ITicker} interface implementation
 *
 * @author Peter.Lawrey
 */
public final class Ticker {
    /**
     * Chosen ticker implementation for this JVM.
     */
    public static final ITicker INSTANCE;

    static {
        if (JNIClock.LOADED) {
            INSTANCE = JNIClock.INSTANCE;
        } else {
            INSTANCE = SystemClock.INSTANCE;
        }
    }

    private Ticker() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Returns the current raw tick count.
     *
     * @return tick value from the active {@link ITicker}
     */
    public static long ticks() {
        return INSTANCE.ticks();
    }

    /**
     * Convenience wrapper to convert the current tick to nanoseconds.
     *
     * @return time in nanoseconds
     */
    public static long nanoTime() {
        return toNanos(ticks());
    }

    /**
     * Converts ticks to nanoseconds using the active ticker.
     *
     * @param ticks tick count from {@link #ticks()}
     * @return equivalent nanoseconds
     */
    public static long toNanos(long ticks) {
        return INSTANCE.toNanos(ticks);
    }

    /**
     * Converts ticks to microseconds using the active ticker.
     *
     * @param ticks tick count from {@link #ticks()}
     * @return equivalent microseconds
     */
    public static double toMicros(long ticks) {
        return INSTANCE.toMicros(ticks);
    }
}
