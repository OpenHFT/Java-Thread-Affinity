/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(LockCheck.isCpuFree(cpu), "cpu should be free before locking: cpu=" + cpu);
        LockCheck.updateCpu(cpu, 0);

        File lockFile = lockChecker.doToFile(cpu);
        assertTrue(lockFile.exists(), "lock file exists: " + lockFile);

        assertTrue(lockFile.delete(), "delete lock file: " + lockFile);
        assertFalse(lockFile.exists(), "lock file deleted: " + lockFile);

        assertFalse(LockCheck.isCpuFree(cpu), "cpu remains locked despite missing file: cpu=" + cpu);
        assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(cpu), "process id recorded for locked cpu=" + cpu);

        LockCheck.releaseLock(cpu);

        assertTrue(LockCheck.isCpuFree(cpu), "cpu should be free after release: cpu=" + cpu);
        LockCheck.updateCpu(cpu, 0);

        lockFile = lockChecker.doToFile(cpu);
        assertTrue(lockFile.exists(), "lock file recreated: " + lockFile);
    }

    @Test
    public void getProcessForCpuReturnsEmptyPidWhenNoFile() throws IOException {
        int freeCpu = 99;
        File lockFile = lockChecker.doToFile(freeCpu);
        assertFalse(lockFile.exists(), "lock file absent: " + lockFile);
        assertEquals(Integer.MIN_VALUE, LockCheck.getProcessForCpu(freeCpu), "no pid when lock file absent");
    }
}
