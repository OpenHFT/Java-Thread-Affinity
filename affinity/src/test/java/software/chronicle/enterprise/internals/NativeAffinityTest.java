/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.BaseAffinityTest;
import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.LinuxJNAAffinity;
import net.openhft.affinity.impl.Utilities;
import org.junit.*;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest extends BaseAffinityTest {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
    }

    @Test
    public void getAffinityCompletesGracefully() {
        System.out.println("affinity: " + Utilities.toBinaryString(getImpl().getAffinity()));
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertFalse("Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty", affinity.isEmpty());
        final int allCoresMask = (1 << CORES) - 1;
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
    public void showOtherIds() {
        System.out.println("processId: " + NativeAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + NativeAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + NativeAffinity.INSTANCE.getCpu());
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() {
        getImpl().setAffinity(CORES_MASK);
    }

    private IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }
}
