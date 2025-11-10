//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.ticker.impl;

import net.openhft.ticker.ITicker;

/**
 * Default implementation, use plain {@link System#nanoTime()}
 *
 * @author cheremin
 * @since 29.12.11,  18:54
 */
public enum SystemClock implements ITicker {
    INSTANCE;

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public long ticks() {
        return nanoTime();
    }

    @Override
    public long toNanos(long ticks) {
        return ticks;
    }

    @Override
    public double toMicros(double ticks) {
        return ticks / 1e3;
    }
}
