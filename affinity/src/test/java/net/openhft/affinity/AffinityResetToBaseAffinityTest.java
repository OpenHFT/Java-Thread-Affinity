/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AffinityResetToBaseAffinityTest extends BaseAffinitySupport {

    @Test
    public void resettingShouldRestoreBaseAffinity() throws Exception {
        assumeTrue(new File("/proc/cpuinfo").exists(), "requires /proc/cpuinfo");

        // initialise CPU layout from the running machine so acquireLock works
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "precondition: base affinity");
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertEquals(1, Affinity.getAffinity().cardinality(), () -> "affinity pinned to a single CPU (cpu=" + lock.cpuId() + ")");

            Affinity.resetToBaseAffinity();
            assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity reset to base");
        }
    }
}
