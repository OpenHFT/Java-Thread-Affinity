/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit test to verify that releasing an {@link AffinityLock} restores the
 * affinity mask back to {@link AffinityLock#BASE_AFFINITY}.
 */
public class AffinityLockReleaseTest extends BaseAffinitySupport {

    @Test
    public void acquireAndReleaseShouldRestoreBaseAffinity() throws Exception {
        assumeTrue(new File("/proc/cpuinfo").exists(), "requires /proc/cpuinfo");

        // initialise CPU layout from the running machine so acquireLock works
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "precondition: base affinity");
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertEquals(1, Affinity.getAffinity().cardinality(), () -> "affinity pinned to a single CPU (cpu=" + lock.cpuId() + ")");
        }
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity restored to base");
    }
}
