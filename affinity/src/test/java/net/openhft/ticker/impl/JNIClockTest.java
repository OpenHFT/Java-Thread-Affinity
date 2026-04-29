/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.ticker.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.BaseAffinityTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Created by Peter Lawrey on 13/07/15.
 */
class JNIClockTest extends BaseAffinityTest {

    @Test
    @Disabled("TODO Fix")
    void testNanoTime() throws InterruptedException {
        for (int i = 0; i < 20000; i++)
            System.nanoTime();
        Affinity.setAffinity(2);

        JNIClock instance = JNIClock.INSTANCE;
        for (int i = 0; i < 50; i++) {
            long start0 = System.nanoTime();
            long start1 = instance.ticks();
            Thread.sleep(10);
            long time0 = System.nanoTime();
            long time1 = instance.ticks();
            if (i > 1) {
                assertEquals(10_100_000, time0 - start0, 100_000);
                assertEquals(10_100_000, instance.toNanos(time1 - start1), 100_000);
                assertEquals(instance.toNanos(time1 - start1) / 1e3, instance.toMicros(time1 - start1), 0.6);
            }
        }
    }

    @Test
    @Disabled("Long running")
    void testJitter() {
        Affinity.setAffinity(2);
        assertEquals(2, Affinity.getCpu());
        int samples = 100000, count = 0;
        long[] time = new long[samples];
        long[] length = new long[samples];

        JNIClock clock = JNIClock.INSTANCE;
        long start = clock.ticks(), prev = start, prevJump = start;
        for (int i = 0; i < 1000_000_000; i++) {
            long now = clock.ticks();
            long delta = now - prev;
            if (delta > 4_000) {
                time[count] = now - prevJump;
                prevJump = now;
                length[count] = delta;
                count++;
                if (count >= samples)
                    break;
            }
            prev = now;
        }
        for (int i = 0; i < count; i++) {
            System.out.println(((long) (clock.toMicros(time[i]) * 10)) / 10.0 + ", " + ((long) (clock.toMicros(length[i]) * 10) / 10.0));
        }
    }
}
