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

/**
 * The SystemClock class provides a default implementation of the ITicker interface
 * using the {@link System#nanoTime()} method for high-resolution time measurement.
 * <p>
 * This implementation directly uses the system's nanosecond time source without any additional processing.
 * </p>
 * <p>
 * Author: cheremin
 * Since: 29.12.11, 18:54
 * </p>
 */
public enum SystemClock implements ITicker {
    INSTANCE;

    /**
     * Returns the current time in nanoseconds using {@link System#nanoTime()}.
     *
     * @return the current time in nanoseconds
     */
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    /**
     * Returns the current time in nanoseconds, same as {@link #nanoTime()}.
     *
     * @return the current time in nanoseconds
     */
    @Override
    public long ticks() {
        return nanoTime();
    }

    /**
     * Converts the given number of ticks to nanoseconds.
     * In this implementation, ticks are already in nanoseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in nanoseconds
     */
    @Override
    public long toNanos(long ticks) {
        return ticks;
    }

    /**
     * Converts the given number of ticks to microseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in microseconds
     */
    @Override
    public double toMicros(double ticks) {
        return ticks / 1e3;
    }
}
