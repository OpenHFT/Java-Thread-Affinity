/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.Utilities;
import net.openhft.affinity.impl.VanillaCpuLayout;
import net.openhft.chronicle.testframework.Waiters;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static net.openhft.affinity.AffinityLock.PROCESSORS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * @author peter.lawrey
 */
public class AffinityLockTest extends BaseAffinityTest {
    private static final Logger logger = LoggerFactory.getLogger(AffinityLockTest.class);

    /**
     * In Java 21 the toString contents of Thread changed to include an ID. This breaks the tests here in Java 21.
     * Strip out the thread ID here so that existing tests continue to pass.
     */
    private static String dumpLocks(AffinityLock[] locks) {
        String value = LockInventory.dumpLocks(locks);
        return value.replaceAll("#[0-9]+(,)?", "");
    }

    @Test
    public void dumpLocksI7() throws IOException {
        LockInventory lockInventory = new LockInventory(VanillaCpuLayout.fromCpuInfo("i7.cpuinfo"));
        AffinityLock[] locks = {
                new AffinityLock(0, 0, true, false, lockInventory),
                new AffinityLock(1, 5, false, false, lockInventory),
                new AffinityLock(2, 6, false, true, lockInventory),
                new AffinityLock(3, 7, false, true, lockInventory),
                new AffinityLock(4, 0, true, false, lockInventory),
                new AffinityLock(5, 1, false, false, lockInventory),
                new AffinityLock(6, 2, false, true, lockInventory),
                new AffinityLock(7, 3, false, true, lockInventory),
        };
        locks[2].assignedThread = new Thread(new InterrupedThread(), "logger");
        locks[2].assignedThread.start();
        locks[3].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[3].assignedThread.start();
        locks[6].assignedThread = new Thread(new InterrupedThread(), "main");
        locks[7].assignedThread = new Thread(new InterrupedThread(), "tcp");
        locks[7].assignedThread.start();
        final String actual = dumpLocks(locks);
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
                new AffinityLock(0, 0, true, false, lockInventory),
                new AffinityLock(1, 3, false, true, lockInventory),
                new AffinityLock(2, 0, true, false, lockInventory),
                new AffinityLock(3, 1, false, true, lockInventory),
        };
        locks[1].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[1].assignedThread.start();
        locks[3].assignedThread = new Thread(new InterrupedThread(), "main");

        final String actual = dumpLocks(locks);
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
                new AffinityLock(0, 0, true, false, lockInventory),
                new AffinityLock(1, 0, false, true, lockInventory),
        };
        locks[1].assignedThread = new Thread(new InterrupedThread(), "engine");
        locks[1].assignedThread.start();

        final String actual = dumpLocks(locks);
        assertEquals("0: General use CPU\n" +
                "1: Thread[engine,5,main] alive=true\n", actual);
        System.out.println(actual);

        locks[1].assignedThread.interrupt();
    }

    @Test
    public void assignReleaseThread() throws IOException {
        if (AffinityLock.RESERVED_AFFINITY.isEmpty()) {
            System.out.println("Cannot run affinity test as no threads gave been reserved.");
            System.out.println("Use isolcpus= in grub.conf or use -D" + AffinityLock.AFFINITY_RESERVED + "={hex mask}");
            return;

        } else if (!new File("/proc/cpuinfo").exists()) {
            System.out.println("Cannot run affinity test as this system doesn't have a /proc/cpuinfo file");
            return;
        }

        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        AffinityLock al = AffinityLock.acquireLock();
        assertEquals(1, Affinity.getAffinity().cardinality());
        al.release();
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
        AffinityLock al2 = AffinityLock.acquireCore();
        assertEquals(1, Affinity.getAffinity().cardinality());
        al2.release();
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
    }

    @Test
    public void resetAffinity() {
        assumeTrue(System.getProperty("os.name").contains("nux"));
        assertTrue(Affinity.getAffinity().cardinality() > 1);
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertEquals(1, Affinity.getAffinity().cardinality());
            assertTrue(lock.resetAffinity());
            lock.resetAffinity(false);
        }
        assertEquals(1, Affinity.getAffinity().cardinality());
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertNotNull(lock);
        }
        assertTrue(Affinity.getAffinity().cardinality() > 1);
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
            if (alForAnotherThread.cpuId() != -1)
                assertNotSame(alForAnotherThread.cpuId(), alForAnotherThread2.cpuId());

            alForAnotherThread2.release();

        } else {
            assertNotSame(alForAnotherThread, al);
            if (alForAnotherThread.cpuId() != -1)
                assertNotSame(alForAnotherThread.cpuId(), al.cpuId());
        }
        alForAnotherThread.release();
        al.release();
    }

    @Test
    public void testIssue19() {
        System.out.println("AffinityLock.PROCESSORS=" + PROCESSORS);

        AffinityLock al = AffinityLock.acquireLock();
        List<AffinityLock> locks = new ArrayList<>();
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
        System.out.println("cpu= " + Affinity.getCpu());
    }

    @Test
    public void testAffinity() throws InterruptedException {
        logger.info("Started");
        displayStatus();
        try (AffinityLock al = AffinityLock.acquireLock()) {
            System.out.println("Main locked");
            displayStatus();
            Thread t = new Thread(() -> {
                AffinityLock al2 = al.acquireLock(AffinityStrategies.SAME_SOCKET, AffinityStrategies.ANY);
                System.out.println("Thread-0 locked");
                displayStatus();
                al2.release();
            });
            t.start();
            t.join();
            System.out.println("Thread-0 unlocked");
            displayStatus();
        }
        System.out.println("All unlocked");
        displayStatus();
    }

    @Test
    public void shouldReturnLockForSpecifiedCpu() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 3);

        try (final AffinityLock affinityLock = AffinityLock.acquireLock(3)) {
            MatcherAssert.assertThat(affinityLock.cpuId(), is(3));
        }
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity());
    }

    @Test
    public void lockFilesShouldBeRemovedOnRelease() {
        if (!Utilities.ISLINUX) {
            return;
        }
        final AffinityLock lock = AffinityLock.acquireLock();

        Path lockFile = Paths.get(System.getProperty("java.io.tmpdir"), "cpu-" + lock.cpuId() + ".lock");
        assertTrue(Files.exists(lockFile));

        lock.release();

        assertFalse(Files.exists(lockFile));
    }

    @Test
    public void wholeCoreLockReservesAllLogicalCpus() throws IOException {
        if (!Utilities.ISLINUX || !new File("/proc/cpuinfo").exists()) {
            return;
        }
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        CpuLayout layout = AffinityLock.cpuLayout();
        try (AffinityLock lock = AffinityLock.acquireCore()) {
            int socketId = layout.socketId(lock.cpuId());
            int coreId = layout.coreId(lock.cpuId());
            for (int i = 0; i < layout.cpus(); i++) {
                if (layout.socketId(i) == socketId && layout.coreId(i) == coreId) {
                    assertFalse(LockCheck.isCpuFree(i), "CPU " + i + " should be reserved");
                }
            }
        }
        for (int i = 0; i < layout.cpus(); i++) {
            assertTrue(LockCheck.isCpuFree(i), "CPU " + i + " should not be reserved");
        }
    }

    private void displayStatus() {
        System.out.println(Thread.currentThread() + " on " + Affinity.getCpu() + "\n" + AffinityLock.dumpLocks());
    }

    @Test
    public void testAffinityLockDescriptions() {
        if (!Utilities.ISLINUX) {
            return;
        }
        try (AffinityLock lock = AffinityLock.acquireLock("last")) {
            assertNotNull(lock);
            assertEquals(PROCESSORS - 1, Affinity.getCpu());
        }
        try (AffinityLock lock = AffinityLock.acquireLock("last")) {
            assertNotNull(lock);
            assertEquals(PROCESSORS - 1, Affinity.getCpu());
        }
        try (AffinityLock lock = AffinityLock.acquireLock("last-1")) {
            assertNotNull(lock);
            assertEquals(PROCESSORS - 2, Affinity.getCpu());
        }
        try (AffinityLock lock = AffinityLock.acquireLock("1")) {
            assertNotNull(lock);
            assertEquals(1, Affinity.getCpu());
        }
        try (AffinityLock lock = AffinityLock.acquireLock("any")) {
            assertTrue(lock.bound);
        }
        try (AffinityLock lock = AffinityLock.acquireLock("none")) {
            assertFalse(lock.bound);
        }
        try (AffinityLock lock = AffinityLock.acquireLock((String) null)) {
            assertFalse(lock.bound);
        }
        try (AffinityLock lock = AffinityLock.acquireLock("0")) {
            assertFalse(lock.bound);
        }
    }

    @Test
    public void acquireLockWithoutBindingDoesNotChangeAffinity() {
        BitSet before = (BitSet) Affinity.getAffinity().clone();
        try (AffinityLock lock = AffinityLock.acquireLock(false)) {
            assertFalse(lock.isBound());
            assertEquals(before, Affinity.getAffinity());
        }
        assertEquals(before, Affinity.getAffinity());
    }

    @Test
    public void testTooHighCpuId() {
        assertFalse(AffinityLock.acquireLock(123456).isBound());
    }

    @Test
    public void testNegativeCpuId() {
        assertFalse(AffinityLock.acquireLock(-1).isBound());
    }

    @Test
    public void testTooHighCpuId2() {
        AffinityLock lock = AffinityLock.acquireLock(new int[]{123456});
        assertFalse(lock.isBound());
    }

    @Test
    public void bindingTwoThreadsToSameCpuThrows() throws InterruptedException {
        assertThrows(IllegalStateException.class, () -> {
            assumeTrue(Runtime.getRuntime().availableProcessors() > 1);

            final AffinityLock lock = AffinityLock.acquireLock(false);
            Thread t = new Thread(() -> {
                lock.bind();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    // ignored
                }
            });
            t.start();

            Waiters.waitForCondition("Waiting for lock to be bound", lock::isBound, 1000);

            try {
                lock.bind();
            } finally {
                t.join();
                lock.release();
            }
        });
    }
}
