/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class CoreReservationBoundaryTest extends BaseAffinityTest {
    @BeforeAll
    static void loadNativeLibraryBeforePerTestTemporaryDirectory() { Affinity.getAffinityImpl(); }

    private LockInventory inventory() throws IOException {
        return new LockInventory(VanillaCpuLayout.fromCpuInfo("i3.cpuinfo")) {
            @Override protected AffinityLock newLock(int id, int pair, boolean base, boolean reservable) {
                return new AffinityLock(id, pair, false, id >= 0, this);
            }
        };
    }

    @Test
    void rejectedCoreStrategiesFallBackWithoutReservingCpus() throws Exception {
        LockInventory inventory = inventory();
        try {
            AffinityLock rejected = inventory.acquireCore(false, 1, (a, b) -> false);
            assertFalse(rejected.isAllocated());
            AffinityLock accepted = inventory.acquireLock(false, AffinityLock.ANY_CPU,
                    (a, b) -> false, AffinityStrategies.ANY);
            assertTrue(accepted.isAllocated());
        } finally { inventory.release(false); }
    }

    @Test
    void unboundWholeCoreReservationReleasesBothLogicalCpus() throws Exception {
        LockInventory inventory = inventory();
        int primary = -1, paired = -1;
        try {
            AffinityLock lock = inventory.acquireCore(false, AffinityLock.ANY_CPU, AffinityStrategies.ANY);
            assertTrue(lock.isAllocated()); primary = lock.cpuId(); paired = lock.cpuId2();
            assertTrue(paired > 0);
            assertNull(inventory.tryAcquireLock(false, paired), "sibling must be reserved even without binding");
            inventory.release(false);
            assertTrue(LockCheck.isCpuFree(primary));
            assertTrue(LockCheck.isCpuFree(paired), "unbound core reservation must release its sibling lock");
        } finally {
            inventory.release(false);
            if (primary >= 0) LockCheck.releaseLock(primary);
            if (paired >= 0) LockCheck.releaseLock(paired);
        }
    }

}
