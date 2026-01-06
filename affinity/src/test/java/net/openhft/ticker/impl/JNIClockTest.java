/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.ticker.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/*
 * Created by Peter Lawrey on 13/07/15.
 */
public class JNIClockTest extends BaseAffinitySupport {

    @Test
    public void testNanoTime() throws InterruptedException {
        assumeTrue(JNIClock.LOADED, "JNIClock native library must be loaded");

        for (int i = 0; i < 20000; i++)
            System.nanoTime();

        JNIClock instance = JNIClock.INSTANCE;
        for (int i = 0; i < 50; i++) {
            long start0 = System.nanoTime();
            long start1 = instance.ticks();
            Thread.sleep(10);
            long time0 = System.nanoTime();
            long time1 = instance.ticks();
            if (i > 1) {
                long deltaSys = time0 - start0;
                long deltaClock = instance.toNanos(time1 - start1);
                assertTrue(deltaSys > 0, "System.nanoTime delta should be positive");
                assertTrue(deltaClock > 0, "JNIClock delta should be positive");

                // The JNI clock should report elapsed time in the same order
                // of magnitude as System.nanoTime, but we allow wide tolerances
                // to avoid flakiness on shared or throttled environments.
                double ratio = (double) deltaClock / (double) deltaSys;
                assertTrue(
                        ratio > 0.1 && ratio < 10.0,
                        "JNIClock and System.nanoTime deltas should be within a reasonable ratio, was " + ratio
                );

                assertEquals(
                        instance.toNanos(time1 - start1) / 1e3,
                        instance.toMicros(time1 - start1),
                        0.6,
                        "toMicros should be consistent with toNanos"
                );
            }
        }
    }

    @Test
    @Disabled("Long running")
    public void testJitter() {
        Affinity.setAffinity(2);
        assertEquals(2, Affinity.getCpu(), "cpu pinned to 2");
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
