/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Immutable, explicitly refreshed observation; it grants no reservation or binding. */
public final class CpuCapabilitySnapshot {
    private final Map<Integer, CpuCapability> cpus;
    private final List<String> diagnostics;

    CpuCapabilitySnapshot(Map<Integer, CpuCapability> cpus, List<String> diagnostics) {
        this.cpus = Collections.unmodifiableMap(new TreeMap<>(cpus));
        this.diagnostics = Collections.unmodifiableList(new ArrayList<>(diagnostics));
    }

    /** Keys are actual logical CPU IDs, including holes in sparse topologies. */
    public Map<Integer, CpuCapability> cpus() { return cpus; }
    public List<String> diagnostics() { return diagnostics; }
}
