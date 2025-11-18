/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class LinuxAffinityParityTest extends BaseAffinityTest {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeClass
    public static void checkEnvironment() {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"));
        assumeTrue("LinuxJNAAffinity must be loaded", LinuxJNAAffinity.LOADED);
        assumeTrue("NativeAffinity must be loaded", NativeAffinity.LOADED);
    }

    @After
    public void resetAffinity() {
        NativeAffinity.INSTANCE.setAffinity(CORES_MASK);
    }

    @Test
    public void jnaAndJniMasksIntersectForSingleCore() {
        for (int core = 0; core < Math.min(CORES, 4); core++) {
            BitSet mask = new BitSet(CORES);
            mask.set(core);

            // Set via JNA, read via JNI
            LinuxJNAAffinity.INSTANCE.setAffinity(mask);
            BitSet jniMask = NativeAffinity.INSTANCE.getAffinity();
            assertTrue("JNI mask should intersect JNA mask for core " + core,
                    jniMask != null && jniMask.intersects(mask));

            // Set via JNI, read via JNA
            NativeAffinity.INSTANCE.setAffinity(mask);
            BitSet jnaMask = LinuxJNAAffinity.INSTANCE.getAffinity();
            assertFalse("JNA mask must not be empty after JNI set", jnaMask.isEmpty());
        }
    }
}

