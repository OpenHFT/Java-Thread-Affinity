/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class NativeAffinityEdgeCaseTest {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeAll
    public static void checkNativeLoaded() {
        String osName = System.getProperty("os.name");
        assumeTrue(osName.startsWith("Linux"), "requires Linux");
        assumeTrue(NativeAffinity.LOADED, "NativeAffinity library must be loaded");
    }

    @AfterEach
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
        assertFalse(affinity.isEmpty(), "affinity mask must be non-empty");
        assertTrue(affinity.length() <= CORES_MASK.length(), "affinity mask length within available cores");
    }

    @Test
    public void setAffinityWithEmptyMaskCompletes() {
        IAffinity impl = NativeAffinity.INSTANCE;
        BitSet empty = new BitSet();
        assertDoesNotThrow(() -> impl.setAffinity(empty), "setAffinity accepts empty mask");
    }

    @Test
    public void setAffinityWithLargeMaskCompletes() {
        IAffinity impl = NativeAffinity.INSTANCE;
        BitSet large = new BitSet(CORES * 4);
        // Intentionally set bits well beyond cpu_set_t size; native code
        // should safely copy only the supported portion.
        large.set(0, CORES * 2, true);
        assertDoesNotThrow(() -> impl.setAffinity(large), "setAffinity accepts large mask");
    }
}
