/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.Utilities;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class NativeAffinityEdgeCaseTest {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeClass
    public static void checkNativeLoaded() {
        String osName = System.getProperty("os.name");
        assumeTrue(osName.startsWith("Linux"));
        assumeTrue("NativeAffinity library must be loaded", NativeAffinity.LOADED);
    }

    @After
    public void resetAffinity() {
        NativeAffinity.INSTANCE.setAffinity(CORES_MASK);
    }

    @Test
    public void getAffinityReturnsNullOrValidMask() {
        IAffinity impl = NativeAffinity.INSTANCE;
        BitSet affinity = impl.getAffinity();
        if (affinity == null) {
            return;
        }
        System.out.println("Native affinity: " + Utilities.toBinaryString(affinity));
        assertFalse("Affinity mask must be non-empty", affinity.isEmpty());
        assertTrue("Affinity mask length must not exceed available cores",
                affinity.length() <= CORES_MASK.length());
    }

    @Test
    public void setAffinityWithEmptyMaskCompletes() {
        IAffinity impl = NativeAffinity.INSTANCE;
        BitSet empty = new BitSet();
        impl.setAffinity(empty);
    }

    @Test
    public void setAffinityWithLargeMaskCompletes() {
        IAffinity impl = NativeAffinity.INSTANCE;
        BitSet large = new BitSet(CORES * 4);
        // Intentionally set bits well beyond cpu_set_t size; native code
        // should safely copy only the supported portion.
        large.set(0, CORES * 2, true);
        impl.setAffinity(large);
    }
}

