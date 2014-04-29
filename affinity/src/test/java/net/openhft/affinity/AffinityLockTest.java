/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.openhft.affinity.AffinityLock.acquireLock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author peter.lawrey
 */
@SuppressWarnings("ALL")
public class AffinityLockTest {
    @Test
    public void dumpLocksI7() throws IOException {
        LockInventory lockInventory = new LockInventory(VanillaCpuLayout.fromCpuInfo("i7.cpuinfo"));
        AffinityLock[] locks = {
                new AffinityLock(0, true, false, lockInventory),
                new AffinityLock(1, false, false, lockInventory),
                new AffinityLock(2, false, true, lockInventory),
                new AffinityLock(3, false, true, lockInventory),
                new AffinityLock(4, true, false, lockInventory),
                new AffinityLock(5, false, false, lockInventory),
                new AffinityLock(6, false, true, lockInventory),
                new AffinityLock(7, false, true, lockInventory),
        };
        locks[2].assignedThread = new Thread(new InterrupedThread(), "logger");
        locks[2].assignedThread.start();
        locks[3].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[3].assignedThread.start();
        locks[6].assignedThread = new Thread(new InterrupedThread(), "main");
        locks[7].assignedThread = new Thread(new InterrupedThread(), "tcp");
        locks[7].assignedThread.start();
        final String actual = LockInventory.dumpLocks(locks);
        assertEquals("0: General use CPU\n" +
                "1: CPU not available\n" +
                "2: Thread[logger,5,main] alive=true\n" +
                "3: Thread[engine,5,main] alive=true\n" +
                "4: General use CPU\n" +
                "5: CPU not available\n" +
                "6: Thread[main,5,main] alive=false\n" +
                "7: Thread[tcp,5,main] alive=true\n", actual);
        System.out.println(actual);

        locks[2].assignedThread.interrupt();
        locks[3].assignedThread.interrupt();
        locks[6].assignedThread.interrupt();
        locks[7].assignedThread.interrupt();
    }

    @Test
    public void dumpLocksI3() throws IOException {
        LockInventory lockInventory = new LockInventory(VanillaCpuLayout.fromCpuInfo("i3.cpuinfo"));
        AffinityLock[] locks = {
                new AffinityLock(0, true, false, lockInventory),
                new AffinityLock(1, false, true, lockInventory),
                new AffinityLock(2, true, false, lockInventory),
                new AffinityLock(3, false, true, lockInventory),
        };
        locks[1].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[1].assignedThread.start();
        locks[3].assignedThread = new Thread(new InterrupedThread(), "main");

        final String actual = LockInventory.dumpLocks(locks);
        assertEquals("0: General use CPU\n" +
                "1: Thread[engine,5,main] alive=true\n" +
                "2: General use CPU\n" +
                "3: Thread[main,5,main] alive=false\n", actual);
        System.out.println(actual);

        locks[1].assignedThread.interrupt();
    }

    @Test
    public void dumpLocksCoreDuo() throws IOException {
        LockInventory lockInventory = new LockInventory(VanillaCpuLayout.fromCpuInfo("core.duo.cpuinfo"));
        AffinityLock[] locks = {
                new AffinityLock(0, true, false, lockInventory),
                new AffinityLock(1, false, true, lockInventory),
        };
        locks[1].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[1].assignedThread.start();

        final String actual = LockInventory.dumpLocks(locks);
        assertEquals("0: General use CPU\n" +
                "1: Thread[engine,5,main] alive=true\n", actual);
        System.out.println(actual);

        locks[1].assignedThread.interrupt();
    }

    @Test
    public void assignReleaseThread() throws IOException {
        if (AffinityLock.RESERVED_AFFINITY == 0) {
            System.out.println("Cannot run affinity test as no threads gave been reserved.");
            System.out.println("Use isolcpus= in grub.conf or use -D" + AffinityLock.AFFINITY_RESERVED + "={hex mask}");
            return;
        } else if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, AffinitySupport.getAffinity());
        AffinityLock al = AffinityLock.acquireLock();
        assertEquals(1, Long.bitCount(AffinitySupport.getAffinity()));
        al.release();
        assertEquals(AffinityLock.BASE_AFFINITY, AffinitySupport.getAffinity());

        assertEquals(AffinityLock.BASE_AFFINITY, AffinitySupport.getAffinity());
        AffinityLock al2 = AffinityLock.acquireCore();
        assertEquals(1, Long.bitCount(AffinitySupport.getAffinity()));
        al2.release();
        assertEquals(AffinityLock.BASE_AFFINITY, AffinitySupport.getAffinity());
    }

    @Test
    public void testIssue21() throws IOException {
        if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());
        AffinityLock al = AffinityLock.acquireLock();
        AffinityLock alForAnotherThread = al.acquireLock(AffinityStrategies.ANY);
        if (Runtime.getRuntime().availableProcessors() > 2) {
            AffinityLock alForAnotherThread2 = al.acquireLock(AffinityStrategies.ANY);
            assertNotSame(alForAnotherThread, alForAnotherThread2);
            assertNotSame(alForAnotherThread.cpuId(), alForAnotherThread2.cpuId());

            alForAnotherThread2.release();
        } else {
            assertNotSame(alForAnotherThread, al);
            assertNotSame(alForAnotherThread.cpuId(), al.cpuId());
        }
        alForAnotherThread.release();
        al.release();
    }

    @Test
    public void testIssue19() {
        AffinityLock al = AffinityLock.acquireLock();
        List<AffinityLock> locks = new ArrayList<AffinityLock>();
        locks.add(al);
        for (int i = 0; i < 256; i++)
            locks.add(al = al.acquireLock(AffinityStrategies.DIFFERENT_SOCKET,
                    AffinityStrategies.DIFFERENT_CORE,
                    AffinityStrategies.SAME_SOCKET,
                    AffinityStrategies.ANY));
        for (AffinityLock lock : locks) {
            lock.release();
        }
    }

    @Test
    public void testGettid() {
        System.out.println("cpu= " + AffinitySupport.getCpu());
    }

    @Test
    public void testAffinity() throws InterruptedException {
        System.out.println("Started");
        displayStatus();
        final AffinityLock al = acquireLock();
        System.out.println("Main locked");
        displayStatus();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                AffinityLock al2 = al.acquireLock(AffinityStrategies.ANY);
                System.out.println("Thread-0 locked");
                displayStatus();
                al2.release();
            }
        });
        t.start();
        t.join();
        System.out.println("Thread-0 unlocked");
        displayStatus();
        al.release();
        System.out.println("All unlocked");
        displayStatus();
    }

    private void displayStatus() {
        System.out.println(Thread.currentThread() + " on " + AffinitySupport.getCpu() + "\n" + AffinityLock.dumpLocks());
    }
}
