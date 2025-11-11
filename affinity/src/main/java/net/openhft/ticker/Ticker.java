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
     * @return The current value of the system timer, in nanoseconds.
     */
    public static long ticks() {
        return INSTANCE.ticks();
    }

    public static long nanoTime() {
        return toNanos(ticks());
    }

    public static long toNanos(long ticks) {
        return INSTANCE.toNanos(ticks);
    }

    public static double toMicros(long ticks) {
        return INSTANCE.toMicros(ticks);
    }
}
