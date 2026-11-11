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
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CpuFrequencyTest {
    @TempDir Path root;
    private void file(String path, String value) throws Exception {
        Path target = root.resolve(path);
        Files.createDirectories(target.getParent());
        Files.write(target, value.getBytes(StandardCharsets.US_ASCII));
    }
    @Test void reportsHardwareMaximumWithIndependentSourceAndUnknownCoreClass() throws Exception {
        file("devices/system/cpu/online", "2,8");
        file("devices/system/cpu/cpu8/cpufreq/cpuinfo_max_freq", "4800000\n");
        file("devices/system/cpu/cpu8/cpufreq/scaling_max_freq", "2400000");
        CpuCapability c = LinuxCpuCapabilities.read(root, CpuIdList.parse("8")).cpus().get(8);
        assertEquals(4800000L, c.maximumFrequencyKhz().getAsLong());
        assertEquals(CpuCapability.CoreType.UNKNOWN, c.coreType());
        assertTrue(c.frequencySource().endsWith("cpu8/cpufreq/cpuinfo_max_freq"));
    }
    @Test void missingHardwareMaximumDoesNotUsePolicyCap() throws Exception {
        file("devices/system/cpu/online", "0");
        file("devices/system/cpu/cpu0/cpufreq/scaling_max_freq", "2000000");
        CpuCapability c = LinuxCpuCapabilities.read(root, CpuIdList.parse("0")).cpus().get(0);
        assertFalse(c.maximumFrequencyKhz().isPresent());
        assertTrue(c.frequencySource().endsWith("cpuinfo_max_freq"));
    }
    @Test void malformedZeroNegativeOverflowAndUnreadableRemainUnknown() throws Exception {
        Path path = root.resolve("frequency");
        for (String value : Arrays.asList("", "0", "-1", "3.5", "4800 MHz", "9223372036854775808")) {
            Files.write(path, value.getBytes(StandardCharsets.US_ASCII));
            List<String> diagnostics = new ArrayList<>();
            assertFalse(CpuFrequency.readMaximum(path, diagnostics).isPresent());
            assertEquals(1, diagnostics.size(), value);
        }
        List<String> diagnostics = new ArrayList<>();
        assertFalse(CpuFrequency.readMaximum(root, diagnostics).isPresent());
        assertEquals(1, diagnostics.size());
    }
    @Test void refreshDoesNotMutateOldSnapshotOrInferClassFromFrequency() throws Exception {
        file("devices/system/cpu/online", "0");
        file("bus/event_source/devices/cpu_core/cpus", "");
        file("bus/event_source/devices/cpu_atom/cpus", "0");
        file("devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "5000000");
        CpuCapability before = LinuxCpuCapabilities.read(root, CpuIdList.parse("0")).cpus().get(0);
        file("devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "4000000");
        CpuCapability after = LinuxCpuCapabilities.read(root, CpuIdList.parse("0")).cpus().get(0);
        assertEquals(5000000L, before.maximumFrequencyKhz().getAsLong());
        assertEquals(4000000L, after.maximumFrequencyKhz().getAsLong());
        assertEquals(CpuCapability.CoreType.EFFICIENCY, before.coreType());
        assertEquals(CpuCapability.CoreType.EFFICIENCY, after.coreType());
    }
}
