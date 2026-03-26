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
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * @author peter.lawrey
 */
class JnaAffinityTest extends BaseAffinityTest {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeAll
    static void checkJniLibraryPresent() {
        assumeTrue(LinuxJNAAffinity.LOADED);
    }

    @Test
    void getAffinityCompletesGracefully() {
        System.out.println("affinity: " + Utilities.toBinaryString(getImpl().getAffinity()));
    }

    @Test
    void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertFalse(affinity.isEmpty(), "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty");
        final int allCoresMask = (1 << CORES) - 1;
        assertTrue(
                affinity.length() <= CORES_MASK.length(),
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")"
        );
    }

    @Test
    void setAffinityCompletesGracefully() {
        BitSet affinity = new BitSet(1);
        affinity.set(0, true);
        getImpl().setAffinity(affinity);
    }

    @Test
    void getAffinityReturnsValuePreviouslySet() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        final IAffinity impl = LinuxJNAAffinity.INSTANCE;
        for (int core = 0; core < CORES; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    @Test
    void showOtherIds() {
        System.out.println("processId: " + LinuxJNAAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + LinuxJNAAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + LinuxJNAAffinity.INSTANCE.getCpu());
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @AfterEach
    void tearDown() {
        getImpl().setAffinity(CORES_MASK);
    }

    private IAffinity getImpl() {
        return LinuxJNAAffinity.INSTANCE;
    }
}
