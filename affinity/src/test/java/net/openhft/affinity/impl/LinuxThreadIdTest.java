/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs(OS.LINUX)
class LinuxThreadIdTest {
    @ParameterizedTest(name = "{0} uses Linux gettid syscall {4}")
    @CsvSource({
            "x86,     false, false, false, 224",
            "x86_64,  false, false, true,  186",
            "arm,     false, true,  false, 224",
            "aarch64, false, true,  true,  178",
            "ppc,     true,  false, false, 207",
            "ppc64,   true,  false, true,  207"
    })
    void selectsLinuxAbiNumber(String architecture, boolean powerPc, boolean arm,
                              boolean is64Bit, int expected) {
        assertEquals(expected, LinuxJNAAffinity.getTidSyscallNumber(powerPc, arm, is64Bit), architecture);
    }

    @Test
    void threadIdMatchesProcThreadSelf() throws IOException {
        Path threadSelf = Paths.get("/proc/thread-self");
        assertTrue(Files.isSymbolicLink(threadSelf), "/proc/thread-self is required for the native TID check");
        int kernelThreadId = Integer.parseInt(Files.readSymbolicLink(threadSelf).getFileName().toString());

        assertEquals(kernelThreadId, LinuxJNAAffinity.INSTANCE.getThreadId());
        assertEquals(kernelThreadId, LinuxJNAAffinity.INSTANCE.getThreadId(), "cached thread ID");
    }
}
