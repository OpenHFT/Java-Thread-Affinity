/*
 * Copyright 2016-2025 chronicle.software
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
 * Abstraction of a high resolution time source used throughout the library.
 * <p>
 * Implementations may be based on {@link System#nanoTime()} or platform
 * specific timers such as the processor&apos;s time stamp counter accessed via
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
