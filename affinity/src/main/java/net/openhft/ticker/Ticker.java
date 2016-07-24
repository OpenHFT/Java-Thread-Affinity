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
        throw new InstantiationError( "Must not instantiate this class" );
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