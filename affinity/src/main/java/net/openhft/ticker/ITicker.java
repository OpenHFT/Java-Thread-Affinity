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

/**
 * The ITicker interface defines methods for high-resolution time measurement.
 * Implementations of this interface provide mechanisms to get the current time in nanoseconds,
 * convert between different time units, and access high-resolution tick counts.
 * <p>
 * Created by Peter Lawrey on 13/07/15.
 * </p>
 */
public interface ITicker {
    /**
     * Returns the current time in nanoseconds.
     *
     * @return the current time in nanoseconds
     */
    long nanoTime();

    /**
     * Returns the current tick count.
     *
     * @return the current tick count
     */
    long ticks();

    /**
     * Converts the given number of ticks to nanoseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in nanoseconds
     */
    long toNanos(long ticks);

    /**
     * Converts the given number of ticks to microseconds.
     *
     * @param ticks the number of ticks
     * @return the equivalent time in microseconds
     */
    double toMicros(double ticks);
}
