/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.BaseAffinityTest;
import net.openhft.affinity.IAffinity;
import org.junit.After;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * @author cheremin
 * @since 29.12.11,  20:25
 */
public abstract class AbstractAffinityImplTest extends BaseAffinityTest {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    protected abstract IAffinity getImpl();

    @Test
    public void getAffinityCompletesGracefully() {
        getImpl().getAffinity();
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertFalse("Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty", affinity.isEmpty());
        final long allCoresMask = (1L << CORES) - 1;
        assertTrue(
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")",
                affinity.length() <= CORES_MASK.length()
        );
    }

    @Test
    public void setAffinityCompletesGracefully() {
        BitSet affinity = new BitSet(1);
        affinity.set(0, true);
        getImpl().setAffinity(affinity);
    }

    @Test
    public void getAffinityReturnsValuePreviouslySet() {
        final IAffinity impl = getImpl();
        for (int core = 0; core < CORES; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
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
                assertTrue("thread id should be positive", tid > 0);
                assertTrue("thread id should be below maxThreadId " + maxThreadId, tid < maxThreadId);
            }
            System.out.printf("gettid took an average of %,d ns, tid=%d%n", time / runs, tid);
        }
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() {
        try {
            getImpl().setAffinity(CORES_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
