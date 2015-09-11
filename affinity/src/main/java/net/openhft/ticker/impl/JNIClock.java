/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.ticker.impl;

import net.openhft.ticker.ITicker;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.logging.Logger;

/**
 * JNI-based implementation, trying to use rdtsc() system call
 * to access the most precise timer available
 *
 * @author cheremin
 * @since 29.12.11,  18:56
 */
public enum JNIClock implements ITicker {
    INSTANCE;

    public static final boolean LOADED;
    private static final Logger LOGGER = Logger.getLogger(JNIClock.class.getName());
    private static final int FACTOR_BITS = 17;
    private static final long START;
    private static long RDTSC_FACTOR = 1 << FACTOR_BITS;
    private static double RDTSC_MICRO_FACTOR = 1e-3;
    private static long CPU_FREQUENCY = 1000;

    static {
        boolean loaded;
        long start;
        try {
            // ensure it is loaded.
            NativeAffinity.INSTANCE.getCpu();

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

    static long tscToNano(final long tsc) {
        return (tsc * RDTSC_FACTOR) >> FACTOR_BITS;
    }

    private static void estimateFrequency(int factor) {
        final long start = System.nanoTime();
        long now;
        while ((now = System.nanoTime()) == start) {
        }

        long end = start + factor * 1000000;
        final long start0 = rdtsc0();
        while ((now = System.nanoTime()) < end) {
        }
        long end0 = rdtsc0();
        end = now;

        RDTSC_FACTOR = ((end - start) << FACTOR_BITS) / (end0 - start0) - 1;
        RDTSC_MICRO_FACTOR = 1e-3 * (end - start) / (end0 - start0);
        CPU_FREQUENCY = (end0 - start0 + 1) * 1000 / (end - start);
    }

    native static long rdtsc0();

    public long nanoTime() {
        return tscToNano(rdtsc0() - START);
    }

    @Override
    public long ticks() {
        return rdtsc0();
    }

    @Override
    public long toNanos(long ticks) {
        return tscToNano(ticks);
    }

    @Override
    public double toMicros(double ticks) {
        return ticks * RDTSC_MICRO_FACTOR;
    }
}