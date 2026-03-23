/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import net.openhft.affinity.IAffinity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

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
        assertFalse(affinity.isEmpty(), "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty");
        final long allCoresMask = (1L << CORES) - 1;
        assertTrue(
                affinity.length() <= CORES_MASK.length(),
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")"
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

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
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
