/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.ticker.impl;

import net.openhft.ticker.ITicker;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.logging.Logger;

/**
 * JNI-based implementation that tries to use the rdtsc() system call
 * to access the most precise timer available.
 * <p>
 * This implementation estimates the CPU frequency and uses it to convert
 * rdtsc ticks to nanoseconds and microseconds.
 * </p>
 * <p>
 * Author: cheremin
 * Since: 29.12.11, 18:56
 * </p>
 */
public enum JNIClock implements ITicker {
    INSTANCE;

    // Indicates whether the native library is loaded
    public static final boolean LOADED;

    // Logger instance for logging messages
    private static final Logger LOGGER = Logger.getLogger(JNIClock.class.getName());

    // Number of bits to shift when converting ticks to nanoseconds
    private static final int FACTOR_BITS = 17;

    // Start value of the rdtsc counter
    private static final long START;

    // Conversion factor from rdtsc ticks to nanoseconds
    private static long RDTSC_FACTOR = 1 << FACTOR_BITS;

    // Conversion factor from rdtsc ticks to microseconds
    private static double RDTSC_MICRO_FACTOR = 1e-3;

    // Estimated CPU frequency in MHz
    private static long CPU_FREQUENCY = 1000;

    static {
        boolean loaded;
        long start;
        try {
            // Ensure the native library is loaded
            NativeAffinity.INSTANCE.getCpu();

            // Estimate the CPU frequency
            estimateFrequency(50);
            estimateFrequency(200);
            LOGGER.info("Estimated clock frequency was " + CPU_FREQUENCY + " MHz");
            start = rdtsc0();
            loaded = true;
        } catch (UnsatisfiedLinkError ule) {
            LOGGER.fine("Unable to find libCEInternals in [" + System.getProperty("java.library.path") + "] " + ule);
            start = 0;
            loaded = false;
        }
        LOADED = loaded;
        START = start;
    }

    /**
     * Converts rdtsc ticks to nanoseconds.
     *
     * @param tsc the number of rdtsc ticks
     * @return the corresponding time in nanoseconds
     */
    static long tscToNano(final long tsc) {
        return (tsc * RDTSC_FACTOR) >> FACTOR_BITS;
    }

    /**
     * Estimates the CPU frequency by measuring the duration of a known number of nanoseconds.
     *
     * @param factor the duration factor in milliseconds
     */
    private static void estimateFrequency(int factor) {
        final long start = System.nanoTime();
        long now;
        while ((now = System.nanoTime()) == start) {
            // Busy-wait until the time changes
        }

        long end = start + factor * 1000000;
        final long start0 = rdtsc0();
        while ((now = System.nanoTime()) < end) {
            // Busy-wait until the end time is reached
        }
        long end0 = rdtsc0();
        end = now;

        RDTSC_FACTOR = ((end - start) << FACTOR_BITS) / (end0 - start0) - 1;
        RDTSC_MICRO_FACTOR = 1e-3 * (end - start) / (end0 - start0);
        CPU_FREQUENCY = (end0 - start0 + 1) * 1000 / (end - start);
    }

    /**
     * Native method to read the current value of the rdtsc counter.
     *
     * @return the current rdtsc value
     */
    native static long rdtsc0();

    /**
     * Returns the current time in nanoseconds since the JNIClock was initialized.
     *
     * @return the current time in nanoseconds
     */
    public long nanoTime() {
        return tscToNano(rdtsc0() - START);
    }

    /**
     * Returns the current rdtsc value.
     *
     * @return the current rdtsc value
     */
    @Override
    public long ticks() {
        return rdtsc0();
    }

    /**
     * Converts rdtsc ticks to nanoseconds.
     *
     * @param ticks the number of rdtsc ticks
     * @return the corresponding time in nanoseconds
     */
    @Override
    public long toNanos(long ticks) {
        return tscToNano(ticks);
    }

    /**
     * Converts rdtsc ticks to microseconds.
     *
     * @param ticks the number of rdtsc ticks
     * @return the corresponding time in microseconds
     */
    @Override
    public double toMicros(double ticks) {
        return ticks * RDTSC_MICRO_FACTOR;
    }
}
