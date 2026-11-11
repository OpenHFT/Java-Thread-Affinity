/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NoCpuLayout;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PerformanceInventoryTest extends BaseAffinityTest {
    private String property;
    @BeforeEach void preference() {
        property = System.getProperty("affinity.disablePerformanceCorePreference");
        System.clearProperty("affinity.disablePerformanceCorePreference");
    }
    @AfterEach void restorePreference() {
        if (property == null) System.clearProperty("affinity.disablePerformanceCorePreference");
        else System.setProperty("affinity.disablePerformanceCorePreference", property);
    }
    private LockInventory inventory() {
        Map<Integer, CpuCapability> cpus = new TreeMap<>();
        cpus.put(1, new CpuCapability(1, CpuCapability.CoreType.PERFORMANCE, "fixture", OptionalLong.empty(), "fixture"));
        cpus.put(3, new CpuCapability(3, CpuCapability.CoreType.EFFICIENCY, "fixture", OptionalLong.empty(), "fixture"));
        CpuCapabilitySnapshot snapshot = new CpuCapabilitySnapshot(cpus, Collections.emptyList());
        return new LockInventory(new NoCpuLayout(4), () -> snapshot) {
            @Override protected AffinityLock newLock(int id, int pair, boolean base, boolean reservable) {
                return new AffinityLock(id, pair, base, id > 0, this);
            }
        };
    }
    @Test void automaticFallsThroughOccupiedTierAndReleases() {
        LockInventory inventory = inventory();
        try {
            assertEquals(1, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId());
            assertEquals(3, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId());
            assertFalse(inventory.acquireLock(false, -1, AffinityStrategies.ANY).isAllocated());
        } finally { inventory.release(false); }
        try { assertEquals(1, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId()); }
        finally { inventory.release(false); }
    }
    @Test void explicitAndLegacyChoicesRemainUnchanged() {
        LockInventory inventory = inventory();
        try { assertEquals(3, inventory.acquireLock(false, 3, AffinityStrategies.ANY).cpuId()); }
        finally { inventory.release(false); }
        System.setProperty("affinity.disablePerformanceCorePreference", "true");
        try { assertEquals(3, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId()); }
        finally { inventory.release(false); }
    }
    private LockInventory mixed(boolean includeSibling) {
        Properties layout = new Properties();
        layout.setProperty("0", "0,0,0"); layout.setProperty("1", "0,1,0");
        layout.setProperty("2", "0,2,0"); layout.setProperty("3", "0,3,0"); layout.setProperty("4", "0,3,1");
        Map<Integer, CpuCapability> cpus = new TreeMap<>();
        for (int id = 1; id <= (includeSibling ? 4 : 3); id++)
            cpus.put(id, new CpuCapability(id, id == 2 ? CpuCapability.CoreType.EFFICIENCY
                    : CpuCapability.CoreType.PERFORMANCE, "fixture", OptionalLong.empty(), "fixture"));
        return new LockInventory(net.openhft.affinity.impl.VanillaCpuLayout.fromProperties(layout),
                () -> new CpuCapabilitySnapshot(cpus, Collections.emptyList())) {
            @Override protected AffinityLock newLock(int id, int pair, boolean base, boolean reservable) {
                return new AffinityLock(id, pair, base, id > 0, this);
            }
        };
    }
    @Test void freePhysicalCoreBeatsOccupiedSibling() {
        LockInventory inventory = mixed(true);
        try {
            assertEquals(4, inventory.acquireLock(false, 4, AffinityStrategies.ANY).cpuId());
            assertEquals(1, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId());
        } finally { inventory.release(false); }
    }
    @Test void wholeCoreCannotWidenMaskToMissingSibling() {
        LockInventory inventory = mixed(false);
        try { assertEquals(1, inventory.acquireCore(false, -1, AffinityStrategies.ANY).cpuId()); }
        finally { inventory.release(false); }
    }
    @Test void concurrentReservationsAreDistinct() throws Exception {
        LockInventory inventory = inventory();
        java.util.concurrent.CountDownLatch acquired = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Integer> task = () -> {
                try {
                    int id = inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId();
                    acquired.countDown();
                    assertTrue(acquired.await(5, java.util.concurrent.TimeUnit.SECONDS));
                    return id;
                } finally { inventory.release(false); }
            };
            java.util.concurrent.Future<Integer> first = pool.submit(task), second = pool.submit(task);
            assertEquals(new HashSet<>(Arrays.asList(1, 3)), new HashSet<>(Arrays.asList(
                    first.get(10, java.util.concurrent.TimeUnit.SECONDS), second.get(10, java.util.concurrent.TimeUnit.SECONDS))));
        } finally { pool.shutdownNow(); }
    }
    @Test void wholeCoreAndStrategyConstraintsUsePreference() {
        LockInventory inventory = inventory();
        try { assertEquals(1, inventory.acquireCore(false, -1, AffinityStrategies.ANY).cpuId()); }
        finally { inventory.release(false); }
        try { assertEquals(3, inventory.acquireCore(false, -1, (reference, candidate) -> candidate == 3).cpuId()); }
        finally { inventory.release(false); }
    }
}
