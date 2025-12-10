/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.testimpl.TestFileLockBasedLockChecker;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static net.openhft.affinity.LockCheck.IS_LINUX;

/**
 * Base harness for exercising {@link LockCheck} behaviour on platforms that expose the PID files.
 * Concrete subclasses provide the starting CPU id so tests can be re-used across environments.
 */
public abstract class AbstractLockCheckTest extends BaseAffinityTest {

    protected final TestFileLockBasedLockChecker lockChecker = new TestFileLockBasedLockChecker();
    protected int cpu;

    /**
     * Skip on non-Linux platforms and capture the starting CPU index for the test run.
     */
    @Before
    public void before() {
        Assume.assumeTrue(IS_LINUX);
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
        Assert.assertTrue(LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);
        Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));
    }

    /**
     * Ensures the current process is visible via {@link LockCheck#isProcessRunning(long)}.
     */
    @Test
    public void testPidOnLinux() {
        Assert.assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));
    }

    /**
     * Replaces a PID entry and confirms the new value is persisted.
     */
    @Test
    public void testReplace() throws IOException {
        cpu++;
        Assert.assertTrue(LockCheck.isCpuFree(cpu + 1));
        LockCheck.replacePid(cpu, 0, 123L);
        Assert.assertEquals(123L, LockCheck.getProcessForCpu(cpu));
    }

    /**
     * Regression guard: reading an empty PID file must not throw.
     */
    @Test
    public void shouldNotBlowUpIfPidFileIsEmpty() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        new RandomAccessFile(file, "rw").setLength(0);

        LockCheck.isCpuFree(cpu);
    }
}
