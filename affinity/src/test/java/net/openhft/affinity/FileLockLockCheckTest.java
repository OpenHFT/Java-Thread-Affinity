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
 * @author Tom Shercliff
 */
public class FileLockLockCheckTest extends BaseAffinityTest {

    private final TestFileLockBasedLockChecker lockChecker = new TestFileLockBasedLockChecker();
    private int cpu = 5;

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
    public void lockFileDeletedWhileHeld() throws Exception {
        cpu++;

        Assert.assertTrue(LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);

        File lockFile = lockChecker.doToFile(cpu);
        Assert.assertTrue(lockFile.exists());

        Assert.assertTrue("Could not delete lock file", lockFile.delete());
        Assert.assertFalse(lockFile.exists());

        Assert.assertFalse("CPU should remain locked despite missing file", LockCheck.isCpuFree(cpu));
        Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));

        LockCheck.releaseLock(cpu);

        Assert.assertTrue("Lock should be free after release", LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);

        lockFile = lockChecker.doToFile(cpu);
        Assert.assertTrue("Lock file should be recreated", lockFile.exists());
    }

    @Test
    public void getProcessForCpuReturnsEmptyPidWhenNoFile() throws IOException {
        int freeCpu = 99;
        File lockFile = lockChecker.doToFile(freeCpu);
        Assert.assertFalse(lockFile.exists());
        Assert.assertEquals(Integer.MIN_VALUE, LockCheck.getProcessForCpu(freeCpu));
    }
}
