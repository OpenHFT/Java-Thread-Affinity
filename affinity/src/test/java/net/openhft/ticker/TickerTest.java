/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.ticker;

import net.openhft.ticker.impl.JNIClock;
import net.openhft.ticker.impl.SystemClock;
import org.junit.Test;

import static org.junit.Assert.*;

public class TickerTest {

    @Test
    public void instanceMatchesLoadedClock() {
        if (JNIClock.LOADED) {
            assertSame("When JNI clock is available it should back Ticker.INSTANCE",
                    JNIClock.INSTANCE, Ticker.INSTANCE);
        } else {
            assertSame("Without JNI support the system clock should be used",
                    SystemClock.INSTANCE, Ticker.INSTANCE);
        }
    }

    @Test
    public void conversionsUseUnderlyingClock() {
        long ticks = Ticker.ticks();
        long nanos = Ticker.toNanos(ticks);
        double micros = Ticker.toMicros(ticks);

        assertTrue("Ticker ticks should never be negative", ticks >= 0);
        assertTrue("Ticker nanos should never be negative", nanos >= 0);
        assertTrue("Ticker micros should never be negative", micros >= 0.0);

        long reference = 123_456_000L;
        double expectedMicros = reference / 1_000.0;

        if (Ticker.INSTANCE == SystemClock.INSTANCE) {
            assertEquals("System clock should treat ticks as nanos", reference, Ticker.toNanos(reference));
            assertEquals("System clock converts nanos to micros using division",
                    expectedMicros, Ticker.toMicros(reference), 0.0001);
        } else {
            long converted = Ticker.toNanos(reference);
            assertEquals("Native clock should convert ticks back to nanos consistently",
                    converted, Ticker.toNanos(reference));
            assertEquals("Native clock micros conversion should align with nanos conversion",
                    converted / 1_000.0, Ticker.toMicros(reference), converted * 0.01);
        }

        long later = Ticker.nanoTime();
        assertTrue("nanoTime should advance monotonically", later >= nanos);
    }
}
