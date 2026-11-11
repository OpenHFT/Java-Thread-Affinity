/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.OptionalLong;

/** OS-reported core class, not a workload-specific performance ranking. */
public final class CpuCapability {
    public enum CoreType { PERFORMANCE, EFFICIENCY, UNKNOWN }
    public enum Confidence { OS_REPORTED, UNKNOWN }

    private final int cpuId;
    private final CoreType coreType;
    private final String source;
    private final OptionalLong maximumFrequencyKhz;
    private final String frequencySource;

    CpuCapability(int cpuId, CoreType coreType, String source, OptionalLong maximumFrequencyKhz, String frequencySource) {
        this.cpuId = cpuId;
        this.coreType = coreType;
        this.source = source;
        this.maximumFrequencyKhz = maximumFrequencyKhz;
        this.frequencySource = frequencySource;
    }

    public int cpuId() { return cpuId; }
    public CoreType coreType() { return coreType; }
    public String source() { return source; }
    /** Kernel-reported maximum in kHz; empty when unavailable, not current speed or a ranking. */
    public OptionalLong maximumFrequencyKhz() { return maximumFrequencyKhz; }
    /** Path read or attempted for the maximum frequency; independent of the core-class source. */
    public String frequencySource() { return frequencySource; }
    public Confidence confidence() {
        return coreType == CoreType.UNKNOWN ? Confidence.UNKNOWN : Confidence.OS_REPORTED;
    }
}
