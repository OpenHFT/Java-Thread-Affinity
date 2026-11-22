/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class RoutineLoggingTest {
    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("0", "0,0,0"); p.setProperty("1", "0,0,1"); p.setProperty("2", "0,1,0");
        VanillaCpuLayout layout = VanillaCpuLayout.fromProperties(p);
        System.out.println("layout=" + layout.cpus() + ":" + layout.threadsPerCore());
        // Make the existing no-isolation branch deterministic in this disposable JVM.
        AffinityLock.RESERVED_AFFINITY.clear();
        AffinityLock.Warnings.warmNoReservedCPUs();
        AffinityLock.acquireLock(-3); // genuine caller error must remain WARN
    }
    private String run(String level) throws Exception {
        Process p = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-XX:ActiveProcessorCount=4", "-Dorg.slf4j.simpleLogger.defaultLogLevel=" + level,
                "-cp", System.getProperty("java.class.path"), getClass().getName())
                .redirectErrorStream(true).start();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096]; int size;
        while ((size = p.getInputStream().read(buffer)) != -1) bytes.write(buffer, 0, size);
        assertEquals(0, p.waitFor());
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
    @Test void infoIsQuietForRoutineMessagesButKeepsRealWarnings() throws Exception {
        String output = run("info");
        assertFalse(output.contains("cpuDetails.size="), output);
        assertFalse(output.contains("No isolated CPUs found"), output);
        assertTrue(output.contains("WARN"), output);
        assertTrue(output.contains("cpuId must be between"), output);
        assertTrue(output.contains("layout=3:2"), output);
    }
    @Test void debugKeepsCountsAndFallbackDetails() throws Exception {
        String output = run("debug");
        assertTrue(output.contains("Non-uniform CPU layout: cpuDetails.size= 3"), output);
        assertTrue(output.contains("No isolated CPUs found"), output);
        assertTrue(output.contains("layout=3:2"), output);
    }
}
