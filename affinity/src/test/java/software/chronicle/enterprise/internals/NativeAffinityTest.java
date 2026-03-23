/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.BaseAffinityTest;
import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.LinuxJNAAffinity;
import net.openhft.affinity.impl.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.*;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest extends BaseAffinityTest {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeAll
    public static void checkJniLibraryPresent() {
        assumeTrue(NativeAffinity.LOADED);
    }

    @Test
    public void getAffinityCompletesGracefully() {
        System.out.println("affinity: " + Utilities.toBinaryString(getImpl().getAffinity()));
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertFalse(affinity.isEmpty(), "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty");
        final int allCoresMask = (1 << CORES) - 1;
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
    @Disabled("TODO AFFINITY-25")
    public void getAffinityReturnsValuePreviouslySet() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        final IAffinity impl = NativeAffinity.INSTANCE;
        for (int core = 0; core < CORES; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    @Test
    @Disabled("TODO AFFINITY-25")
    public void JNAwithJNI() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        int nbits = Runtime.getRuntime().availableProcessors();
        BitSet affinity = new BitSet(nbits);
        affinity.set(1);
        NativeAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity2 = LinuxJNAAffinity.INSTANCE.getAffinity();
        assertEquals(1, NativeAffinity.INSTANCE.getCpu());
        assertEquals(affinity, affinity2);

        affinity.clear();
        affinity.set(2);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity3 = NativeAffinity.INSTANCE.getAffinity();
        assertEquals(2, LinuxJNAAffinity.INSTANCE.getCpu());
        assertEquals(affinity, affinity3);

        affinity.set(0, nbits);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
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

    @AfterEach
    public void tearDown() {
        getImpl().setAffinity(CORES_MASK);
    }

    private IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }
}
