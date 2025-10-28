/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.NoCpuLayout;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class LockInventoryLoggingTest {

    @Test
    public void acquireLockFallsBackWhenLockFileCannotBeCreated() throws IOException {
        Assume.assumeTrue("Lock inventory relies on Linux file locks", LockCheck.IS_LINUX);

        String originalTmpDir = System.getProperty("java.io.tmpdir");
        File notADirectory = File.createTempFile("affinity-locks", ".tmp");

        try {
            System.setProperty("java.io.tmpdir", notADirectory.getAbsolutePath());
            LockInventory inventory = new LockInventory(new NoCpuLayout(4));

            AffinityLock lock = inventory.acquireLock(true, 1, AffinityStrategies.ANY);

            assertNotNull(lock);
            assertFalse("Lock should be marked as not allocated when acquisition fails", lock.isAllocated());
            assertFalse("Thread should not be interrupted after IOException", Thread.currentThread().isInterrupted());
        } finally {
            System.setProperty("java.io.tmpdir", originalTmpDir);
            //noinspection ResultOfMethodCallIgnored
            notADirectory.delete();
        }
    }

    @Test
    public void releaseClearsStaleAssignments() throws Exception {
        LockInventory inventory = new LockInventory(new NoCpuLayout(2));
        AffinityLock[] locks = accessLogicalLocks(inventory);
        AffinityLock lock = locks[0];
        lock.assignedThread = new Thread("dead-worker");
        lock.bound = true;

        inventory.release(false);

        assertNull("Assigned thread should be cleared for inactive threads", lock.assignedThread);
        assertFalse("Lock should be unbound after release", lock.isBound());
    }

    private static AffinityLock[] accessLogicalLocks(LockInventory inventory) throws Exception {
        Field logicalLocksField = LockInventory.class.getDeclaredField("logicalCoreLocks");
        logicalLocksField.setAccessible(true);
        return (AffinityLock[]) logicalLocksField.get(inventory);
    }
}
