/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Tom Shercliff
 */
public class FileLockLockCheckTest extends AbstractLockCheckTest {

    @Override
    protected int initialCpu() {
        return 5;
    }

    @Test
    public void lockFileDeletedWhileHeld() throws Exception {
        cpu++;

        Assert.assertTrue("CPU should be free before locking", LockCheck.isCpuFree(cpu));
        LockCheck.updateCpu(cpu, 0);

        File lockFile = lockChecker.doToFile(cpu);
        Assert.assertTrue(lockFile.exists());

        Assert.assertTrue("Could not delete lock file", lockFile.delete());
        Assert.assertFalse(lockFile.exists());

        Assert.assertFalse("CPU should remain locked despite missing file", LockCheck.isCpuFree(cpu));
        Assert.assertEquals("process ID should still be recorded for locked CPU",
                LockCheck.getPID(), LockCheck.getProcessForCpu(cpu));

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
