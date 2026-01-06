/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.testimpl.FileLockBasedLockCheckerStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static net.openhft.affinity.LockCheck.IS_LINUX;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Base harness for exercising {@link LockCheck} behaviour on platforms that expose the PID files.
 * Concrete subclasses provide the starting CPU id so tests can be re-used across environments.
 */
public abstract class AbstractLockCheckTest extends BaseAffinitySupport {

    protected final FileLockBasedLockCheckerStub lockChecker = new FileLockBasedLockCheckerStub();
    protected int cpu;

    /**
     * Skip on non-Linux platforms and capture the starting CPU index for the test run.
     */
    @BeforeEach
    public void before() {
        assumeTrue(IS_LINUX, "requires Linux lock files");
        cpu = initialCpu();
    }

    /**
     * Provides a CPU index that should be free for the duration of the test.
     */
    protected abstract int initialCpu();

    /**
     * Verifies that a free CPU can be claimed and reported via the PID map.
     */
    @Test
    public void test() throws IOException {
        assertTrue(LockCheck.isCpuFree(cpu), "cpu should be free: cpu=" + cpu);
        LockCheck.updateCpu(cpu, 0);
        assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu), "process id recorded for cpu=" + cpu);
    }

    /**
     * Ensures the current process is visible via {@link LockCheck#isProcessRunning(long)}.
     */
    @Test
    public void testPidOnLinux() {
        assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()), "current process should be running");
    }

    /**
     * Replaces a PID entry and confirms the new value is persisted.
     */
    @Test
    public void testReplace() throws IOException {
        cpu++;
        assertTrue(LockCheck.isCpuFree(cpu + 1), "cpu should be free: cpu=" + (cpu + 1));
        LockCheck.replacePid(cpu, 0, 123L);
        assertEquals(123L, LockCheck.getProcessForCpu(cpu), "pid replaced for cpu=" + cpu);
    }

    /**
     * Regression guard: reading an empty PID file must not throw.
     */
    @Test
    public void shouldNotBlowUpIfPidFileIsEmpty() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        new RandomAccessFile(file, "rw").setLength(0);

        assertDoesNotThrow(() -> LockCheck.isCpuFree(cpu), "empty PID file should not throw");
    }
}
