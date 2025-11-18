/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.ticker.impl;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class JNIClockBasicBehaviourTest {

    @BeforeClass
    public static void checkLoaded() {
        assumeTrue("JNIClock native library must be loaded", JNIClock.LOADED);
    }

    @Test
    public void ticksEventuallyChange() {
        JNIClock clock = JNIClock.INSTANCE;
        long first = clock.ticks();
        long different = first;
        for (int i = 0; i < 1000 && different == first; i++) {
            different = clock.ticks();
        }
        assertTrue("ticks should eventually change", different != first);
    }

    @Test
    public void nanoTimeIncreasesOverSleep() throws Exception {
        JNIClock clock = JNIClock.INSTANCE;
        long start = clock.nanoTime();
        Thread.sleep(5L);
        long end = clock.nanoTime();
        assertTrue("nanoTime should increase over sleep", end > start);
    }

    @Test
    public void concurrentTicksDoesNotThrow() throws Exception {
        final JNIClock clock = JNIClock.INSTANCE;
        int threads = 4;
        int iterations = 10_000;
        Thread[] ts = new Thread[threads];
        Runnable r = () -> {
            for (int i = 0; i < iterations; i++) {
                clock.ticks();
            }
        };
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(r, "jniclock-basic-" + i);
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
    }
}

