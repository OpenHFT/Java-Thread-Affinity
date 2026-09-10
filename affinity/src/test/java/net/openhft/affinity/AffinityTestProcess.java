/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

abstract class AffinityTestProcess {
    @TempDir
    Path directory;

    static String testClasspath() {
        return System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
    }

    String runProbe(Class<?> probe, String scenario, boolean withJna, String... options) throws Exception {
        String classpath = Arrays.stream(testClasspath().split(File.pathSeparator))
                .filter(entry -> withJna || !new File(entry).getName().startsWith("jna-"))
                .collect(Collectors.joining(File.pathSeparator));
        Path output = directory.resolve(scenario + ".log");
        List<String> command = new ArrayList<>();
        command.add(new File(System.getProperty("java.home"), "bin/java").getAbsolutePath());
        command.addAll(Arrays.asList(options));
        // Avoid Java 8 launcher argument-conversion warnings before the checked JNI calls.
        command.add("-Daffinity.test.scenario=" + scenario);
        command.add("-cp");
        command.add(classpath);
        command.add(probe.getName());
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        boolean finished;
        try {
            finished = process.waitFor(30, TimeUnit.SECONDS);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
            }
        }
        String log = new String(Files.readAllBytes(output), StandardCharsets.UTF_8);
        assertTrue(finished, () -> "Timed out: " + command + "\n" + log);
        assertEquals(0, process.exitValue(), () -> "Failed: " + command + "\n" + log);
        assertFalse(log.contains("WARNING in native method"), log);
        assertFalse(log.contains("FATAL ERROR in native method"), log);
        System.out.print(log);
        assumeFalse(log.contains("SKIP " + scenario), log);
        assertTrue(log.contains("PASS " + scenario), log);
        return log;
    }
}
