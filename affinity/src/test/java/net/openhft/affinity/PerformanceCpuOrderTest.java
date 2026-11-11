/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PerformanceCpuOrderTest {
    private CpuCapabilitySnapshot snapshot(String classes) {
        Map<Integer, CpuCapability> cpus = new TreeMap<>();
        for (int i = 0; i < classes.length(); i++) {
            char c = classes.charAt(i);
            if (c == '-') continue;
            CpuCapability.CoreType type = c == 'P' ? CpuCapability.CoreType.PERFORMANCE
                    : c == 'E' ? CpuCapability.CoreType.EFFICIENCY : CpuCapability.CoreType.UNKNOWN;
            cpus.put(i, new CpuCapability(i, type, "fixture", OptionalLong.of(c == 'E' ? 9000000 : 1000000), "fixture"));
        }
        return new CpuCapabilitySnapshot(cpus, Collections.emptyList());
    }
    @Test void classNotIdOrMaximumFrequencyDeterminesPreference() {
        assertEquals(Arrays.asList(1, 3, 2), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-PEE"), id -> true).ids);
        assertEquals(Arrays.asList(3, 2, 1), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-EEP"), id -> true).ids);
    }
    @Test void freeCoreBeatsSmtSiblingWithinTierNotAcrossTiers() {
        assertEquals(Arrays.asList(1, 2, 3), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot("-PPE"), id -> id != 2).ids);
    }
    @Test void unknownAndHomogeneousRetainLegacyOrdering() {
        for (String classes : Arrays.asList("-P?E", "-PPP", "-EEE"))
            assertEquals(Arrays.asList(3, 2, 1), PerformanceCpuOrder.choose(Arrays.asList(3, 2, 1), snapshot(classes), id -> id != 3).ids);
    }
    @Test void neverWidensEnvelopeAndKeepsSparseIds() {
        assertEquals(Arrays.asList(1, 3), PerformanceCpuOrder.choose(Arrays.asList(8, 3, 2, 1), snapshot("-P-E"), id -> true).ids);
        assertTrue(PerformanceCpuOrder.choose(Arrays.asList(8), snapshot("-P-E"), id -> true).ids.isEmpty());
    }
}
