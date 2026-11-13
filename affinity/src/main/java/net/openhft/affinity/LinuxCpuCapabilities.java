/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Explicit Linux Intel-hybrid PMU observation; no native binding or allocation side effects. */
public final class LinuxCpuCapabilities {
    private static final String ONLINE = "devices/system/cpu/online";
    private static final String CORE = "bus/event_source/devices/cpu_core/cpus";
    private static final String ATOM = "bus/event_source/devices/cpu_atom/cpus";

    private LinuxCpuCapabilities() { }

    /**
     * Intersects sysfs online CPUs with the caller's allowed envelope. Obtain that
     * envelope before narrowing thread affinity; this method cannot reconstruct it.
     * Missing online information returns an empty snapshot with a diagnostic.
     */
    public static CpuCapabilitySnapshot read(BitSet allowedCpus) {
        return read(Paths.get("/sys"), allowedCpus);
    }

    /**
     * Reads a sysfs root, also suitable for recorded fixtures. The observation is
     * not atomic across hotplug; callers must revalidate eligibility before binding.
     * Empty PMU lists are valid; unavailable or malformed lists yield unknown classes.
     */
    public static CpuCapabilitySnapshot read(Path sysfsRoot, BitSet allowedCpus) {
        Objects.requireNonNull(sysfsRoot, "sysfsRoot");
        BitSet eligible = (BitSet) Objects.requireNonNull(allowedCpus, "allowedCpus").clone();
        List<String> diagnostics = new ArrayList<>();
        BitSet online = readList(sysfsRoot.resolve(ONLINE), diagnostics);
        Map<Integer, CpuCapability> cpus = new TreeMap<>();
        if (online == null) return new CpuCapabilitySnapshot(cpus, diagnostics);
        eligible.and(online);
        BitSet core = readList(sysfsRoot.resolve(CORE), diagnostics);
        BitSet atom = readList(sysfsRoot.resolve(ATOM), diagnostics);
        for (int cpu = eligible.nextSetBit(0); cpu >= 0; cpu = eligible.nextSetBit(cpu + 1)) {
            CpuCapability.CoreType type = CpuCapability.CoreType.UNKNOWN;
            String source = "unknown: incomplete PMU lists";
            if (core != null && atom != null) {
                boolean p = core.get(cpu), e = atom.get(cpu);
                if (p != e) {
                    type = p ? CpuCapability.CoreType.PERFORMANCE : CpuCapability.CoreType.EFFICIENCY;
                    source = sysfsRoot.resolve(p ? CORE : ATOM).toString();
                } else {
                    source = p ? "unknown: conflicting PMU membership" : "unknown: absent PMU membership";
                    if (p) diagnostics.add("CPU " + cpu + ": conflicting PMU membership");
                }
            }
            Path frequencyPath = sysfsRoot.resolve("devices/system/cpu/cpu" + cpu + "/cpufreq/cpuinfo_max_freq");
            cpus.put(cpu, new CpuCapability(cpu, type, source,
                    CpuFrequency.readMaximum(frequencyPath, diagnostics), frequencyPath.toString()));
        }
        return new CpuCapabilitySnapshot(cpus, diagnostics);
    }

    private static BitSet readList(Path path, List<String> diagnostics) {
        try {
            return CpuIdList.parse(new String(Files.readAllBytes(path), StandardCharsets.US_ASCII));
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            diagnostics.add(path + ": unavailable or invalid CPU list (" + e.getClass().getSimpleName() + ")");
            return null;
        }
    }
}
