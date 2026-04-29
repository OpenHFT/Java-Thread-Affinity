/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.testimpl.TestFileLockBasedLockChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import static net.openhft.affinity.LockCheck.IS_LINUX;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * @author Rob Austin.
 */
class LockCheckTest extends BaseAffinityTest {

    private final TestFileLockBasedLockChecker lockChecker = new TestFileLockBasedLockChecker();
    private int cpu = 11;

    @BeforeEach
    void before() {
        assumeTrue(IS_LINUX);
    }

    @Test
    void test() throws IOException {
        assertTrue(LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);
        assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));
    }

    @Test
    void testPidOnLinux() {
        assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));
    }

    @Test
    void testNegativePidOnLinux() {
        assertFalse(LockCheck.isProcessRunning(-1));
    }

    @Test
    void testReplace() throws IOException {
        cpu++;
        assertTrue(LockCheck.isCpuFree(cpu + 1));
        LockCheck.replacePid(cpu, 0, 123L);
        assertEquals(123L, LockCheck.getProcessForCpu(cpu));
    }

    @Test
    void shouldNotBlowUpIfPidFileIsEmpty() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        new RandomAccessFile(file, "rw").setLength(0);

        LockCheck.isCpuFree(cpu);
    }

    @Test
    void shouldNotBlowUpIfPidFileIsCorrupt() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        try (final FileWriter writer = new FileWriter(file, false)) {
            writer.append("not a number\nnot a date");
        }

        LockCheck.isCpuFree(cpu);
    }
}
