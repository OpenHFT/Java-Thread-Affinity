/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class NumaLocalityTest extends BaseAffinityTest {
    @TempDir Path root;
    private void node(int id, String cpus) throws Exception {
        Path p = root.resolve("devices/system/node/node" + id + "/cpulist");
        Files.createDirectories(p.getParent()); Files.write(p, cpus.getBytes(StandardCharsets.US_ASCII));
    }
    private CpuCapabilitySnapshot snapshot(String types) {
        Map<Integer, CpuCapability> cpus = new TreeMap<>();
        for (int id = 1; id < types.length(); id++) cpus.put(id, new CpuCapability(id,
                types.charAt(id) == 'P' ? CpuCapability.CoreType.PERFORMANCE : CpuCapability.CoreType.EFFICIENCY,
                "fixture", OptionalLong.empty(), "fixture"));
        return new CpuCapabilitySnapshot(cpus, Collections.emptyList());
    }
    @Test void cpuZeroNodeNotNodeNumberZeroIsTheProxy() throws Exception {
        node(0, "2-3"); node(7, "0-1");
        NumaLocality n = NumaLocality.read(root);
        assertTrue(n.known()); assertTrue(n.contains(1)); assertFalse(n.contains(3));
        assertEquals(root.resolve("devices/system/node/node7/cpulist").toString(), n.source);
    }
    @Test void localWithinClassAndLocalOnHomogeneousMachine() throws Exception {
        node(1, "0-1"); node(2, "2-3");
        NumaLocality n = NumaLocality.read(root);
        assertEquals(Arrays.asList(1, 3, 2), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-PPP"), id -> true, n).ids);
        // Local E must not outrank a remote P: class preference has priority.
        assertEquals(Arrays.asList(3, 1, 2), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-EEP"), id -> true, n).ids);
        assertEquals(Arrays.asList(1, 3, 2), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-PEP"), id -> true, n).ids);
    }
    @Test void unavailableMalformedConflictingOrAbsentCpuZeroAreUnknown() throws Exception {
        assertFalse(NumaLocality.read(root).known());
        node(0, "2-3"); assertFalse(NumaLocality.read(root).known());
        node(1, "0,bad"); assertFalse(NumaLocality.read(root).known());
        node(1, "0,2"); assertFalse(NumaLocality.read(root).known());
    }
    @Test void automaticInventoryUsesLocalNodeThenRemoteFallback() throws Exception {
        node(0, "0-1"); node(1, "2-3");
        String old = System.getProperty("affinity.disablePerformanceCorePreference");
        System.clearProperty("affinity.disablePerformanceCorePreference");
        LockInventory inventory = new LockInventory(new net.openhft.affinity.impl.NoCpuLayout(4),
                () -> snapshot("-PPP"), () -> NumaLocality.read(root)) {
            @Override protected AffinityLock newLock(int id, int pair, boolean base, boolean reservable) {
                return new AffinityLock(id, pair, base, id == 1 || id == 3, this);
            }
        };
        try {
            boolean linux = System.getProperty("os.name", "").startsWith("Linux");
            assertEquals(linux ? 1 : 3, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId());
            assertEquals(linux ? 3 : 1, inventory.acquireLock(false, -1, AffinityStrategies.ANY).cpuId());
        } finally {
            inventory.release(false);
            if (old == null) System.clearProperty("affinity.disablePerformanceCorePreference");
            else System.setProperty("affinity.disablePerformanceCorePreference", old);
        }
    }
    @Test void localityDoesNotWidenTheEligibleEnvelope() throws Exception {
        node(0, "0-1"); node(1, "2-3");
        assertEquals(Arrays.asList(3, 2), PerformanceCpuOrder.choose(Arrays.asList(3, 2), snapshot("-PPP"), id -> true, NumaLocality.read(root)).ids);
    }
}
