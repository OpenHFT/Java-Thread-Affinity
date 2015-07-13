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

package net.openhft.clock.impl;

import net.openhft.clock.IClock;

/**
 * Default implementation, use plain {@link System#nanoTime()}
 *
 * @author cheremin
 * @since 29.12.11,  18:54
 */
public enum SystemClock implements IClock {
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