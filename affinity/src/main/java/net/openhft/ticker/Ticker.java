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

package net.openhft.ticker;

import net.openhft.ticker.impl.JNIClock;
import net.openhft.ticker.impl.SystemClock;

/**
 * The Ticker class provides a static factory for obtaining an implementation of the {@link ITicker} interface.
 * It automatically selects the most appropriate implementation based on the availability of native libraries.
 * <p>
 * Author: Peter Lawrey
 * </p>
 */
public final class Ticker {
    // Singleton instance of ITicker
    public static final ITicker INSTANCE;

    static {
        // Select the appropriate ITicker implementation
        if (JNIClock.LOADED) {
            INSTANCE = JNIClock.INSTANCE;
        } else {
            INSTANCE = SystemClock.INSTANCE;
        }
    }

    // Private constructor to prevent instantiation
    private Ticker() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Returns the current value of the system timer in nanoseconds.
     *
     * @return the current value of the system timer, in nanoseconds
     */
    public static long ticks() {
        return INSTANCE.ticks();
    }

    /**
     * Returns the current time in nanoseconds.
     *
     * @return the current time in nanoseconds
     */
    public static long nanoTime() {
        return toNanos(ticks());
    }

    /**
     * Converts the given number of ticks to nanoseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in nanoseconds
     */
    public static long toNanos(long ticks) {
        return INSTANCE.toNanos(ticks);
    }

    /**
     * Converts the given number of ticks to microseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in microseconds
     */
    public static double toMicros(long ticks) {
        return INSTANCE.toMicros(ticks);
    }
}
