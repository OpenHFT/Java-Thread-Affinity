/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.Utilities;
import net.openhft.affinity.impl.VanillaCpuLayout;
import net.openhft.chronicle.testframework.Waiters;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author peter.lawrey
 */
public class AffinityLockTest extends BaseAffinitySupport {
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
                "7: Thread[tcp,5,main] alive=true\n", actual, "lock dump should correctly represent i7 CPU topology with reserved and assigned CPUs");
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
                "3: Thread[main,5,main] alive=false\n", actual, "lock dump should correctly represent i3 CPU topology with hyperthreading configuration");
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
                "1: Thread[engine,5,main] alive=true\n", actual, "lock dump should correctly represent Core Duo single-core CPU topology");
        System.out.println(actual);

        locks[1].assignedThread.interrupt();
    }

    @Test
    public void assignReleaseThread() throws IOException {
        assumeFalse(AffinityLock.RESERVED_AFFINITY.isEmpty(), "requires reserved CPUs (see " + AffinityLock.AFFINITY_RESERVED + ")");
        assumeTrue(new File("/proc/cpuinfo").exists(), "requires /proc/cpuinfo");

        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity should start at base configuration before acquiring any locks");
        try (AffinityLock al = AffinityLock.acquireLock()) {
            assertEquals(1, Affinity.getAffinity().cardinality(), () -> "acquiring a lock should pin thread affinity to exactly one CPU (cpu=" + al.cpuId() + ")");
        }
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity should be restored to base configuration after releasing lock");

        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity should start at base configuration before acquiring core lock");
        try (AffinityLock al2 = AffinityLock.acquireCore()) {
            assertEquals(1, Affinity.getAffinity().cardinality(), () -> "acquiring a core lock should pin thread affinity to exactly one CPU (cpu=" + al2.cpuId() + ")");
        }
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity should be restored to base configuration after releasing core lock");
    }

    @Test
    public void resetAffinity() {
        assumeTrue(System.getProperty("os.name").contains("nux"), "requires Linux");
        assertTrue(Affinity.getAffinity().cardinality() > 1, "system should have multiple CPUs available for affinity testing");
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertEquals(1, Affinity.getAffinity().cardinality(), "acquiring lock should pin affinity to single CPU");
            assertTrue(lock.resetAffinity(), "resetAffinity should return true when successfully resetting to base affinity");
            lock.resetAffinity(false);
        }
        assertEquals(1, Affinity.getAffinity().cardinality(), "affinity should remain pinned to single CPU after resetAffinity(false)");
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            assertNotNull(lock, "should be able to acquire a new lock after partial reset");
        }
        assertTrue(Affinity.getAffinity().cardinality() > 1, "affinity should be restored to multi-CPU base configuration after full release");
    }

    @Test
    public void testIssue21() throws IOException {
        assumeTrue(new File("/proc/cpuinfo").exists(), "requires /proc/cpuinfo");
        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());
        AffinityLock al = AffinityLock.acquireLock();
        AffinityLock alForAnotherThread = al.acquireLock(AffinityStrategies.ANY);
        if (Runtime.getRuntime().availableProcessors() > 2) {
            AffinityLock alForAnotherThread2 = al.acquireLock(AffinityStrategies.ANY);
            assertNotSame(alForAnotherThread, alForAnotherThread2, "acquiring multiple locks should return distinct lock instances");
            if (alForAnotherThread.cpuId() != -1) {
                assertNotEquals(alForAnotherThread.cpuId(), alForAnotherThread2.cpuId(), "distinct locks should be assigned to different CPU ids");
            }

            alForAnotherThread2.release();

        } else {
            assertNotSame(alForAnotherThread, al, "acquiring a derived lock should return a distinct instance from parent lock");
            if (alForAnotherThread.cpuId() != -1) {
                assertNotEquals(alForAnotherThread.cpuId(), al.cpuId(), "parent and child locks should be assigned to different CPU ids");
            }
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
        assertEquals(257, locks.size(), "should successfully acquire 257 locks using fallback strategies without exhausting lock pool");
        for (AffinityLock lock : locks) {
            lock.release();
        }
    }

    @Test
    public void testGettid() {
        int cpu = Affinity.getCpu();
        System.out.println("cpu= " + cpu);
        assertTrue(cpu >= -1, "getCpu should return a valid CPU id (non-negative) or -1 when unavailable");
    }

    @Test
    public void testAffinity() throws InterruptedException {
        BitSet before = (BitSet) Affinity.getAffinity().clone();
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
        assertEquals(before, Affinity.getAffinity(), "affinity should be restored to original configuration after all locks released");
    }

    @Test
    public void shouldReturnLockForSpecifiedCpu() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 3, "requires >3 CPUs");

        try (final AffinityLock affinityLock = AffinityLock.acquireLock(3)) {
            assertEquals(3, affinityLock.cpuId(), "acquireLock with explicit CPU id should bind to that specific CPU");
        }
        assertEquals(AffinityLock.BASE_AFFINITY, Affinity.getAffinity(), "affinity should be restored to base configuration after releasing explicit CPU lock");
    }

    @Test
    public void lockFilesShouldBeRemovedOnRelease() {
        if (!Utilities.ISLINUX) {
            return;
        }
        Path lockFile;
        try (AffinityLock lock = AffinityLock.acquireLock()) {
            lockFile = Paths.get(System.getProperty("java.io.tmpdir"), "cpu-" + lock.cpuId() + ".lock");
            assertTrue(Files.exists(lockFile), "lock file should exist in temp directory while lock is held: " + lockFile);
        }
        assertFalse(Files.exists(lockFile), "lock file should be automatically removed after lock is released: " + lockFile);
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
                    assertFalse(LockCheck.isCpuFree(i), "acquireCore should reserve all logical CPUs on the same physical core, including hyperthreading siblings: CPU " + i);
                }
            }
        }
        for (int i = 0; i < layout.cpus(); i++) {
            assertTrue(LockCheck.isCpuFree(i), "all CPUs should be released after core lock is closed: CPU " + i);
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
            assertEquals(PROCESSORS - 1, lock.cpuId(), "string descriptor 'last' should acquire lock on highest numbered CPU");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("last")) {
            assertEquals(PROCESSORS - 1, lock.cpuId(), "string descriptor 'last' should consistently return same CPU on repeated calls");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("last-1")) {
            assertEquals(PROCESSORS - 2, lock.cpuId(), "string descriptor 'last-1' should acquire lock on second-to-last CPU");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("1")) {
            assertEquals(1, lock.cpuId(), "numeric string descriptor should acquire lock on that specific CPU id");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("any")) {
            assertTrue(lock.bound, "string descriptor 'any' should bind to an available CPU");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("none")) {
            assertFalse(lock.bound, "string descriptor 'none' should create unbound lock without pinning to CPU");
        }
        try (AffinityLock lock = AffinityLock.acquireLock((String) null)) {
            assertFalse(lock.bound, "null string descriptor should create unbound lock without pinning to CPU");
        }
        try (AffinityLock lock = AffinityLock.acquireLock("0")) {
            assertFalse(lock.bound, "string descriptor '0' should create unbound lock as CPU 0 is reserved for general use");
        }
    }

    @Test
    public void acquireLockWithoutBindingDoesNotChangeAffinity() {
        BitSet before = (BitSet) Affinity.getAffinity().clone();
        try (AffinityLock lock = AffinityLock.acquireLock(false)) {
            assertFalse(lock.isBound(), "acquireLock(false) should create an unbound lock that does not pin to a CPU");
            assertEquals(before, Affinity.getAffinity(), "thread affinity should remain unchanged while holding an unbound lock");
        }
        assertEquals(before, Affinity.getAffinity(), "thread affinity should remain unchanged after releasing an unbound lock");
    }

    @Test
    public void testTooHighCpuId() {
        assertFalse(AffinityLock.acquireLock(123456).isBound(), "acquireLock should create unbound lock when requested CPU id exceeds available processors");
    }

    @Test
    public void testNegativeCpuId() {
        assertFalse(AffinityLock.acquireLock(-1).isBound(), "acquireLock should create unbound lock when given negative CPU id");
    }

    @Test
    public void testTooHighCpuId2() {
        AffinityLock lock = AffinityLock.acquireLock(new int[]{123456});
        assertFalse(lock.isBound(), "acquireLock should create unbound lock when CPU id array contains invalid processor numbers");
    }

    @Test
    public void bindingTwoThreadsToSameCpuThrows() throws InterruptedException {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1, "requires >1 CPU");

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
            assertThrows(IllegalStateException.class, () -> lock.bind(), "attempting to bind the same lock from a second thread should throw IllegalStateException");
        } finally {
            t.join();
            lock.release();
        }
    }
}
