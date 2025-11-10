//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.ticker;

/**
 * Abstraction of a high resolution time source used throughout the library.
 * <p>
 * Implementations may be based on {@link System#nanoTime()} or platform
 * specific timers such as the processor's time stamp counter accessed via
 * JNI.  The {@linkplain #ticks() tick values} returned are therefore
 * implementation dependent.  They always increase monotonically but the unit
 * they represent can vary from nanoseconds to CPU cycles.
 * <p>
 * Utility methods are provided to convert these raw ticks into conventional
 * time units.  For example {@link #toNanos(long)} converts the supplied number
 * of ticks to nanoseconds and {@link #toMicros(double)} converts them to
 * microseconds.
 * <p>
 * This interface is typically accessed via the {@link net.openhft.ticker.Ticker}
 * helper class which selects the best available implementation for the
 * running platform.
 */
public interface ITicker {
    long nanoTime();

    long ticks();

    long toNanos(long ticks);

    double toMicros(double ticks);
}
