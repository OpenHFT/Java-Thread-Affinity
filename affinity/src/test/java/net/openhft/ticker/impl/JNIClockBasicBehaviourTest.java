/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.ticker.impl;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JNIClockBasicBehaviourTest {

    @BeforeAll
    public static void checkLoaded() {
        assumeTrue(JNIClock.LOADED, "JNIClock native library must be loaded");
    }

    @Test
    public void ticksEventuallyChange() {
        JNIClock clock = JNIClock.INSTANCE;
        long first = clock.ticks();
        long different = first;
        for (int i = 0; i < 1000 && different == first; i++) {
            different = clock.ticks();
        }
        assertNotEquals(first, different, "ticks should eventually change");
    }

    @Test
    public void nanoTimeIncreasesOverSleep() throws Exception {
        JNIClock clock = JNIClock.INSTANCE;
        long start = clock.nanoTime();
        Thread.sleep(5L);
        long end = clock.nanoTime();
        assertTrue(end > start, "nanoTime should increase over sleep");
    }

    @Test
    public void concurrentTicksDoesNotThrow() throws Exception {
        final JNIClock clock = JNIClock.INSTANCE;
        int threads = 4;
        int iterations = 10_000;
        Thread[] ts = new Thread[threads];
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Runnable r = () -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    clock.ticks();
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        };
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(r, "jniclock-basic-" + i);
            ts[i].start();
        }
        for (Thread t : ts) {
            t.join();
        }
        Throwable thrown = failure.get();
        assertNull(thrown, () -> "ticks threw: " + thrown);
    }
}
