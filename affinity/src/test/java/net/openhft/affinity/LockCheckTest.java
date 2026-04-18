/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.testimpl.TestFileLockBasedLockChecker;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import static net.openhft.affinity.LockCheck.IS_LINUX;

/**
 * @author Rob Austin.
 */
public class LockCheckTest extends BaseAffinityTest {

    private final TestFileLockBasedLockChecker lockChecker = new TestFileLockBasedLockChecker();
    private int cpu = 11;

    @Before
    public void before() {
        Assume.assumeTrue(IS_LINUX);
    }

    @Test
    public void test() throws IOException {
        Assert.assertTrue(LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);
        Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));
    }

    @Test
    public void testPidOnLinux() {
        Assert.assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));
    }

    @Test
    public void testNegativePidOnLinux() {
        Assert.assertFalse(LockCheck.isProcessRunning(-1));
    }

    @Test
    public void testReplace() throws IOException {
        cpu++;
        Assert.assertTrue(LockCheck.isCpuFree(cpu + 1));
        LockCheck.replacePid(cpu, 0, 123L);
        Assert.assertEquals(123L, LockCheck.getProcessForCpu(cpu));
    }

    @Test
    public void shouldNotBlowUpIfPidFileIsEmpty() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        new RandomAccessFile(file, "rw").setLength(0);

        LockCheck.isCpuFree(cpu);
    }

    @Test
    public void shouldNotBlowUpIfPidFileIsCorrupt() throws Exception {
        LockCheck.updateCpu(cpu, 0);

        final File file = lockChecker.doToFile(cpu);
        try (final FileWriter writer = new FileWriter(file, false)) {
            writer.append("not a number\nnot a date");
        }

        LockCheck.isCpuFree(cpu);
    }
}
