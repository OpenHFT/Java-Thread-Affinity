/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class LinuxAffinityParityTest extends BaseAffinitySupport {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeAll
    public static void checkEnvironment() {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"), "requires Linux");
        assumeTrue(LinuxJNAAffinity.LOADED, "requires LinuxJNAAffinity to be loaded");
        assumeTrue(NativeAffinity.LOADED, "requires NativeAffinity to be loaded");
    }

    @AfterEach
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
            assertTrue(jniMask != null && jniMask.intersects(mask), "JNI mask intersects JNA mask for core=" + core);

            // Set via JNI, read via JNA
            NativeAffinity.INSTANCE.setAffinity(mask);
            BitSet jnaMask = LinuxJNAAffinity.INSTANCE.getAffinity();
            assertFalse(jnaMask.isEmpty(), "JNA mask not empty after JNI set");
        }
    }
}
