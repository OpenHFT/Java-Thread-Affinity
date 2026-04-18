/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class AffinityResetToBaseAffinityTest extends BaseAffinityTest {

    @Test
    public void resettingShouldRestoreBaseAffinity() throws Exception {
        if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }

        // initialise CPU layout from the running machine so acquireLock works
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        AffinityLock lock = AffinityLock.acquireLock();
        try {
            assertEquals(1, Affinity.getAffinity().cardinality());

            Affinity.resetToBaseAffinity();
            assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        } finally {
            lock.release();
        }
    }
}
