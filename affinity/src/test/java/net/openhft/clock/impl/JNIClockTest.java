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

import net.openhft.affinity.AffinitySupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 13/07/15.
 */
public class JNIClockTest {

    @Test
    public void testNanoTime() throws Exception {
        for (int i = 0; i < 20000; i++)
            System.nanoTime();
        AffinitySupport.setAffinity(2);

        for (int i = 0; i < 50; i++) {
            long start0 = System.nanoTime();
            long start1 = JNIClock.INSTANCE.ticks();
            Thread.sleep(10);
            long time0 = System.nanoTime();
            long time1 = JNIClock.INSTANCE.ticks();
            if (i > 1) {
                assertEquals(10_100_000, time0 - start0, 100_000);
                assertEquals(10_100_000, JNIClock.INSTANCE.toNanos(time1 - start1), 100_000);
                assertEquals(JNIClock.INSTANCE.toNanos(time1 - start1) / 1e3, JNIClock.INSTANCE.toMicros(time1 - start1), 0.6);
            }
        }
    }
}