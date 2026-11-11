/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class CpuCapabilitiesCliTest {
    @TempDir Path root;
    private void file(String path, String text) throws Exception {
        Path p = root.resolve(path); Files.createDirectories(p.getParent()); Files.write(p, text.getBytes(StandardCharsets.UTF_8));
    }
    private String run(int expected, String... args) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream stream = new PrintStream(output, true, "UTF-8")) {
            assertEquals(expected, CpuCapabilitiesCli.run(args, stream, stream));
        }
        return output.toString("UTF-8");
    }
    @Test void sparseOfflineAndRestrictedMaskWithMaximum() throws Exception {
        file("devices/system/cpu/online", "2,8");
        file("bus/event_source/devices/cpu_core/cpus", "2,8");
        file("bus/event_source/devices/cpu_atom/cpus", "");
        file("devices/system/cpu/cpu8/cpufreq/cpuinfo_max_freq", "4800000");
        String first = run(0, "--allowed", "8-9", "--sysfs-root", root.toString());
        assertTrue(first.contains("8\tPERFORMANCE\tOS_REPORTED"));
        assertTrue(first.contains("\t4800000\t"));
        assertFalse(first.contains("2\tPERFORMANCE")); assertFalse(first.contains("9\t"));
        assertEquals(first, run(0, "--allowed", "8-9", "--sysfs-root", root.toString()));
    }
    @Test void unknownDiagnosticsAndEscapedSources() throws Exception {
        file("devices/system/cpu/online", "8");
        String out = run(3, "--allowed", "8", "--sysfs-root", root.toString());
        assertTrue(out.contains("8\tUNKNOWN\tUNKNOWN"));
        assertTrue(out.contains("diagnostic\t"));
        assertEquals("tab\\tnewline\\nreturn\\rslash\\\\",
                CpuCapabilitiesCli.escape("tab\tnewline\nreturn\rslash\\"));
        assertTrue(out.contains("\tunknown\t"));
    }
    @Test void publicMainReportsIncompleteWithoutWritingFixture() throws Exception {
        file("sentinel", "unchanged");
        Path output = Files.createTempFile("aff10-output", ".txt");
        try {
            Process process = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                    "-cp", System.getProperty("java.class.path"), CpuCapabilitiesCli.class.getName(),
                    "--allowed", "8", "--sysfs-root", root.toString()).redirectErrorStream(true)
                    .redirectOutput(output.toFile()).start();
            assertTrue(process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(3, process.exitValue());
            assertTrue(new String(Files.readAllBytes(output), StandardCharsets.UTF_8).contains("status\tincomplete"));
            assertEquals("unchanged", new String(Files.readAllBytes(root.resolve("sentinel")), StandardCharsets.UTF_8));
            try (java.util.stream.Stream<Path> entries = Files.list(root)) { assertEquals(1, entries.count()); }
        } finally { Files.deleteIfExists(output); }
    }
    @Test void argumentsAndMissingOnlineHaveDifferentStatus() throws Exception {
        assertTrue(run(2, "--allowed", "bad").contains("argument_error"));
        run(2); run(2, "--allowed", ""); run(2, "--unknown", "8");
        assertTrue(run(3, "--allowed", "8", "--sysfs-root", root.toString()).contains("status\tincomplete"));
        assertTrue(run(0, "--help").contains("Usage:"));
    }
}
