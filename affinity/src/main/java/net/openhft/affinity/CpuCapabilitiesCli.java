/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;

/** Read-only tab-separated observations. Requires an explicit caller-supplied CPU envelope. */
public final class CpuCapabilitiesCli {
    private CpuCapabilitiesCli() { }
    public static void main(String[] args) { System.exit(run(args, System.out, System.err)); }

    static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            if (args.length == 1 && "--help".equals(args[0])) {
                out.println("Usage: CpuCapabilitiesCli --allowed IDS [--sysfs-root PATH]");
                return 0;
            }
            if ((args.length != 2 && args.length != 4) || !"--allowed".equals(args[0])
                    || (args.length == 4 && !"--sysfs-root".equals(args[2])))
                throw new IllegalArgumentException("expected --allowed IDS [--sysfs-root PATH]");
            BitSet allowed = CpuIdList.parse(args[1]);
            if (allowed.isEmpty()) throw new IllegalArgumentException("empty allowed envelope");
            Path root = args.length == 4 ? Paths.get(args[3]) : Paths.get("/sys");
            CpuCapabilitySnapshot snapshot = LinuxCpuCapabilities.read(root, allowed);
            out.println("allowed_source\tcaller_supplied");
            out.println("allowed_ids\t" + allowed);
            out.println("cpu\tclass\tconfidence\tclass_source\tmaximum_khz\tfrequency_source");
            for (CpuCapability cpu : snapshot.cpus().values()) {
                out.println(cpu.cpuId() + "\t" + cpu.coreType() + "\t" + cpu.confidence() + "\t"
                        + escape(cpu.source()) + "\t"
                        + (cpu.maximumFrequencyKhz().isPresent() ? cpu.maximumFrequencyKhz().getAsLong() : "unknown")
                        + "\t" + escape(cpu.frequencySource()));
            }
            for (String diagnostic : snapshot.diagnostics()) err.println("diagnostic\t" + escape(diagnostic));
            boolean incomplete = snapshot.cpus().isEmpty() || !snapshot.diagnostics().isEmpty()
                    || snapshot.cpus().values().stream().anyMatch(c -> c.coreType() == CpuCapability.CoreType.UNKNOWN);
            out.println("status\t" + (incomplete ? "incomplete" : "observed"));
            return incomplete ? 3 : 0;
        } catch (IllegalArgumentException invalid) {
            err.println("argument_error\t" + escape(invalid.getMessage()));
            return 2;
        }
    }
    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n");
    }
}
