/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.BaseAffinitySupport;
import net.openhft.affinity.IAffinity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author cheremin
 * @since 29.12.11,  20:25
 */
public abstract class AbstractAffinityImplTest extends BaseAffinitySupport {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    protected abstract IAffinity getImpl();

    @Test
    public void getAffinityCompletesGracefully() {
        assertNotNull(getImpl().getAffinity(), "getAffinity returns a BitSet");
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertFalse(affinity.isEmpty(), () -> "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty");
        final long allCoresMask = (1L << CORES) - 1;
        assertTrue(
                affinity.length() <= CORES_MASK.length(),
                () -> "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")"
        );
    }

    @Test
    public void setAffinityCompletesGracefully() {
        BitSet affinity = new BitSet(1);
        affinity.set(0, true);
        assertDoesNotThrow(() -> getImpl().setAffinity(affinity), "setAffinity completes");
    }

    @Test
    public void getAffinityReturnsValuePreviouslySet() {
        final IAffinity impl = getImpl();
        for (int core = 0; core < CORES; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            impl.setAffinity(mask);
            final BitSet actual = impl.getAffinity();
            assertEquals(mask, actual, "affinity round-trip for core=" + core);
        }
    }

    protected void runThreadIdBenchmark(int maxThreadId) {
        System.out.println("pid=" + getImpl().getProcessId());
        System.out.println("tid=" + getImpl().getThreadId());
        Affinity.setThreadId();

        for (int j = 0; j < 3; j++) {
            final int runs = 100000;
            long tid = 0;
            long time = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                @SuppressWarnings("deprecation")
                long tid0 = Thread.currentThread().getId();
                tid = tid0;
                time += System.nanoTime() - start;
                assertTrue(tid > 0, "thread id should be positive");
                assertTrue(tid < maxThreadId, "thread id should be below maxThreadId " + maxThreadId);
            }
            System.out.printf("gettid took an average of %,d ns, tid=%d%n", time / runs, tid);
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            getImpl().setAffinity(CORES_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
