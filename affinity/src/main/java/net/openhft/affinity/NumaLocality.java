/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.BitSet;

/** CPU 0's NUMA node is a locality proxy, not an observation of kernel scheduling. */
final class NumaLocality {
    private final BitSet local;
    final String source;
    private NumaLocality(BitSet local, String source) {
        this.local = (BitSet) local.clone();
        this.source = source;
    }
    static NumaLocality unknown() { return new NumaLocality(new BitSet(), "numa_unknown"); }
    boolean known() { return !local.isEmpty(); }
    boolean contains(int id) { return local.get(id); }

    static NumaLocality read(Path sysfs) {
        Path nodes = sysfs.resolve("devices/system/node");
        BitSet seen = new BitSet();
        NumaLocality result = unknown();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(nodes, "node*")) {
            for (Path node : paths) {
                if (!node.getFileName().toString().matches("node[0-9]+")) continue;
                Path path = node.resolve("cpulist");
                BitSet cpus = CpuIdList.parse(new String(Files.readAllBytes(path), StandardCharsets.US_ASCII).trim());
                if (seen.intersects(cpus)) return unknown();
                seen.or(cpus);
                if (cpus.get(0)) result = new NumaLocality(cpus, path.toString());
            }
            return result;
        } catch (IOException | SecurityException | IllegalArgumentException unavailable) {
            return unknown();
        }
    }
}
