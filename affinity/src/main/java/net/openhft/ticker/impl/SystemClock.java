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
 * Default implementation, use plain {@link System#nanoTime()}
 *
 * @author cheremin
 * @since 29.12.11,  18:54
 */
public enum SystemClock implements ITicker {
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