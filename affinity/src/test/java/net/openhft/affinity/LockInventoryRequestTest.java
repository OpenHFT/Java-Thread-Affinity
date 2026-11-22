/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Specific CPU requests outside the layout, and the warning written when a requested CPU
 * is unavailable and another one is taken instead. Uses bind=false so no thread is moved.
 */
class LockInventoryRequestTest extends BaseAffinityTest {

    private LockInventory inventory;
    private int cpus;

    @BeforeEach
    void createInventory() throws IOException {
        assumeFalse(Affinity.getAffinityImpl() instanceof NullAffinity);
        // every CPU except 0 is reservable, so the fallback search is deterministic
        inventory = new LockInventory(VanillaCpuLayout.fromCpuInfo("i7.cpuinfo")) {
            @Override
            protected AffinityLock newLock(int cpuId, int cpuId2, boolean base, boolean reservable) {
                return super.newLock(cpuId, cpuId2, base, cpuId > 0);
            }
        };
        cpus = inventory.getCpuLayout().cpus();
        assertEquals(8, cpus);
    }

    @Test
    void requestEqualToCpuCountReturnsNoLock() {
        String log = captureStderr(() -> {
            AffinityLock lock = inventory.acquireLock(false, cpus, AffinityStrategies.ANY);
            assertFalse(lock.isAllocated());
        });
        assertTrue(log.contains("Unable to acquire lock on CPU " + cpus), log);
        assertTrue(log.contains("out of range 0.." + (cpus - 1)), log);
        assertTrue(log.contains("no CPU taken"), log);
    }

    @Test
    void tryAcquireEqualToCpuCountReturnsNull() {
        String log = captureStderr(() -> assertNull(inventory.tryAcquireLock(false, cpus)));
        assertTrue(log.contains("Unable to acquire lock on CPU " + cpus), log);
        assertTrue(log.contains("out of range"), log);
    }

    @Test
    void negativeNonSentinelRequestReturnsNoLock() {
        assertNotEquals(AffinityLock.ANY_CPU, -2);
        String log = captureStderr(() -> {
            assertFalse(inventory.acquireLock(false, -2, AffinityStrategies.ANY).isAllocated());
            assertNull(inventory.tryAcquireLock(false, -2));
        });
        assertTrue(log.contains("Unable to acquire lock on CPU -2"), log);
        assertTrue(log.contains("out of range"), log);
    }

    @Test
    void fallbackNamesHoldingThreadAndCpuTaken() throws InterruptedException {
        assumeTrue(LockCheck.IS_LINUX);
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<AffinityLock> holderLock = new AtomicReference<>();
        Thread holder = new Thread(() -> {
            holderLock.set(inventory.acquireLock(false, 3, AffinityStrategies.ANY));
            held.countDown();
            try {
                done.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cpu3-holder");
        holder.start();
        try {
            assertTrue(held.await(10, TimeUnit.SECONDS));
            assertEquals(3, holderLock.get().cpuId());

            AtomicReference<AffinityLock> taken = new AtomicReference<>();
            String log = captureStderr(() -> taken.set(inventory.acquireLock(false, 3, AffinityStrategies.ANY)));

            assertEquals(7, taken.get().cpuId(), "existing fallback policy is unchanged");
            assertTrue(log.contains("Unable to acquire lock on CPU 3"), log);
            assertTrue(log.contains("held by thread"), log);
            assertTrue(log.contains("cpu3-holder"), log);
            assertTrue(log.contains("using CPU 7 instead"), log);
        } finally {
            done.countDown();
            holder.join(10_000);
        }
    }

    @Test
    void fallbackNamesHoldingPid() throws IOException {
        assumeTrue(LockCheck.IS_LINUX);
        // a lock file owned by another process id, with no thread of this inventory assigned
        assertTrue(LockCheck.replacePid(3, 0, 123L));

        AtomicReference<AffinityLock> taken = new AtomicReference<>();
        String log = captureStderr(() -> taken.set(inventory.acquireLock(false, 3, AffinityStrategies.ANY)));

        assertEquals(7, taken.get().cpuId());
        assertTrue(log.contains("Unable to acquire lock on CPU 3"), log);
        assertTrue(log.contains("held by pid 123"), log);
        assertTrue(log.contains("using CPU 7 instead"), log);
    }

    @Test
    void fallbackNamesStrategyRefusal() {
        AffinityStrategy notCpu3 = (requested, candidate) -> candidate != 3;

        AtomicReference<AffinityLock> taken = new AtomicReference<>();
        String log = captureStderr(() -> taken.set(inventory.acquireLock(false, 3, notCpu3)));

        assertEquals(7, taken.get().cpuId());
        assertTrue(log.contains("Unable to acquire lock on CPU 3"), log);
        assertTrue(log.contains("not allowed by the requested strategies"), log);
        assertTrue(log.contains("using CPU 7 instead"), log);
    }

    @Test
    void explicitListFailureNamesHolderAndUnpinnedResult() throws IOException {
        assumeTrue(LockCheck.IS_LINUX);
        assumeTrue(AffinityLock.PROCESSORS > 1);
        // the only listed CPU is held through its lock file by another process id
        assertTrue(LockCheck.replacePid(1, 0, 123L));

        AtomicReference<AffinityLock> taken = new AtomicReference<>();
        String log = captureStderr(() -> taken.set(AffinityLock.acquireLock(new int[]{1})));

        assertFalse(taken.get().isAllocated());
        assertTrue(log.contains("Unable to acquire lock on CPU 1"), log);
        assertTrue(log.contains("held by pid 123"), log);
        assertFalse(log.contains("trying to find another CPU"), log);
        assertTrue(log.contains("Failed to lock any CPU in explicit list [1]"), log);
        assertTrue(log.contains("not pinned"), log);
    }

    private static String captureStderr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        System.setErr(new PrintStream(bytes, true));
        try {
            action.run();
        } finally {
            System.setErr(original);
        }
        String log = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        original.print(log);
        return log;
    }
}
