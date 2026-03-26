/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify that releasing an {@link AffinityLock} restores the
 * affinity mask back to {@link AffinityLock#BASE_AFFINITY}.
 */
class AffinityLockReleaseTest extends BaseAffinityTest {

    @Test
    void acquireAndReleaseShouldRestoreBaseAffinity() throws Exception {
        if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }

        // initialise CPU layout from the running machine so acquireLock works
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        AffinityLock lock = AffinityLock.acquireLock();
        assertEquals(1, Affinity.getAffinity().cardinality());
        lock.release();
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
    }
}
