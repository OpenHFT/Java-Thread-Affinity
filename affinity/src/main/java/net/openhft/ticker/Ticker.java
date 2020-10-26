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