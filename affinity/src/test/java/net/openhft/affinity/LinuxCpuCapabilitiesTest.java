/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.TreeSet;
import static net.openhft.affinity.CpuCapability.CoreType.*;
import static org.junit.jupiter.api.Assertions.*;

class LinuxCpuCapabilitiesTest {
    @TempDir Path root;
    private void file(String path, String value) throws Exception {
        Path target = root.resolve(path);
        Files.createDirectories(target.getParent());
        Files.write(target, value.getBytes(StandardCharsets.US_ASCII));
    }
    private void fixture(String online, String p, String e) throws Exception {
        file("devices/system/cpu/online", online);
        if (p != null) file("bus/event_source/devices/cpu_core/cpus", p);
        if (e != null) file("bus/event_source/devices/cpu_atom/cpus", e);
    }
    private CpuCapabilitySnapshot read(String allowed) {
        return LinuxCpuCapabilities.read(root, CpuIdList.parse(allowed));
    }
    @Test void sparseRestrictedAndOfflineCpus() throws Exception {
        fixture("0,2,4,8", "0,2", "4,6,8");
        CpuCapabilitySnapshot s = read("2,4,6");
        assertEquals(new TreeSet<>(Arrays.asList(2,4)), s.cpus().keySet());
        assertEquals(PERFORMANCE, s.cpus().get(2).coreType());
        assertEquals(EFFICIENCY, s.cpus().get(4).coreType());
        assertEquals(CpuCapability.Confidence.OS_REPORTED, s.cpus().get(4).confidence());
        assertTrue(s.cpus().get(2).source().endsWith("cpu_core/cpus"));
        assertTrue(s.diagnostics().isEmpty());
    }
    @Test void cpuNumbersDoNotDetermineClass() throws Exception {
        fixture("0,21", "21", "0");
        assertEquals(PERFORMANCE, read("0,21").cpus().get(21).coreType());
        assertEquals(EFFICIENCY, read("0,21").cpus().get(0).coreType());
    }
    @Test void efficiencyDoesNotInventLowPowerSubtype() throws Exception {
        fixture("12-21", "", "12-21");
        for (CpuCapability c : read("12-21").cpus().values()) assertEquals(EFFICIENCY, c.coreType());
    }
    @Test void missingOrMalformedPmuListMakesClassificationUnknown() throws Exception {
        fixture("0-1", "0", null);
        assertEquals(UNKNOWN, read("0").cpus().get(0).coreType());
        file("bus/event_source/devices/cpu_atom/cpus", "1,broken");
        assertEquals(UNKNOWN, read("0").cpus().get(0).coreType());
        assertFalse(read("0").diagnostics().isEmpty());
    }
    @Test void overlappingAndUnlistedMembershipRemainUnknown() throws Exception {
        fixture("0-2", "0-1", "1");
        CpuCapabilitySnapshot s = read("0-2");
        assertEquals(PERFORMANCE, s.cpus().get(0).coreType());
        assertEquals(UNKNOWN, s.cpus().get(1).coreType());
        assertEquals(UNKNOWN, s.cpus().get(2).coreType());
        assertEquals(CpuCapability.Confidence.UNKNOWN, s.cpus().get(1).confidence());
        assertTrue(s.diagnostics().get(0).contains("conflicting"));
    }
    @Test void missingOrInvalidOnlineSetDoesNotWidenEligibility() throws Exception {
        assertTrue(read("0-4").cpus().isEmpty());
        file("devices/system/cpu/online", "0-3,garbage");
        assertTrue(read("0-4").cpus().isEmpty());
        assertFalse(read("0-4").diagnostics().isEmpty());
    }
    @Test void homogeneousUnknownHostIsNotAssumedPerformance() throws Exception {
        fixture("0-3", null, null);
        assertEquals(4, read("0-3").cpus().size());
        for (CpuCapability c : read("0-3").cpus().values()) assertEquals(UNKNOWN, c.coreType());
    }
    @Test void emptyAllowedSetNeverSelectsCpu() throws Exception {
        fixture("0-3", "0-1", "2-3");
        assertTrue(read("").cpus().isEmpty());
    }
    @Test void snapshotsAreImmutableAndRefreshIsExplicit() throws Exception {
        fixture("0-1", "0", "1");
        BitSet allowed = CpuIdList.parse("0-1");
        CpuCapabilitySnapshot s = LinuxCpuCapabilities.read(root, allowed);
        assertEquals(CpuIdList.parse("0-1"), allowed);
        allowed.clear();
        file("devices/system/cpu/online", "1");
        assertEquals(2, s.cpus().size());
        assertEquals(1, read("0-1").cpus().size());
        assertThrows(UnsupportedOperationException.class, () -> s.cpus().clear());
        assertThrows(UnsupportedOperationException.class, () -> s.diagnostics().add("changed"));
    }
    @Test void invalidCpuListsAreRejectedWithoutPartialAcceptance() {
        for (String value : Arrays.asList("-1", "3-1", "1,", "1,,2", "1-2-3", "2147483647", "999999999999", "1 2"))
            assertThrows(IllegalArgumentException.class, () -> CpuIdList.parse(value), value);
        assertEquals(CpuIdList.parse("0-2"), CpuIdList.parse("0,1-2,2\n"));
    }
    @Test void unreadableSourceIsAnExplicitUnknown() throws Exception {
        fixture("0", "0", null);
        Files.createDirectory(root.resolve("bus/event_source/devices/cpu_atom"));
        Files.createDirectory(root.resolve("bus/event_source/devices/cpu_atom/cpus"));
        assertEquals(UNKNOWN, read("0").cpus().get(0).coreType());
        assertFalse(read("0").diagnostics().isEmpty());
    }
}
