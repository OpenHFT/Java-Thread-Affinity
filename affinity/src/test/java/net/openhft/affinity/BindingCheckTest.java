/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import org.junit.jupiter.api.Test;
import java.util.BitSet;
import static org.junit.jupiter.api.Assertions.*;

class BindingCheckTest {
    private AffinityLock lock(int cpu, boolean bound) {
        AffinityLock lock = new AffinityLock(cpu, 0, false, true, null);
        lock.assignedThread = Thread.currentThread();
        lock.bound = bound;
        return lock;
    }
    private IAffinity backend(BitSet mask) {
        return new IAffinity() {
            public BitSet getAffinity() { return mask; }
            public void setAffinity(BitSet ignored) { fail("must not bind"); }
            public int getCpu() { fail("sampled CPU is not binding evidence"); return -1; }
            public int getProcessId() { return 1; }
            public int getThreadId() { return 1; }
        };
    }
    @Test void verifiesSingletonAndDefendsObservedMask() {
        BitSet mask = new BitSet(); mask.set(8);
        AffinityLock lock = lock(8, true);
        BindingCheck check = BindingCheck.inspect(lock, backend(mask));
        assertEquals(BindingCheck.Status.VERIFIED, check.status());
        mask.clear(); check.observedMask().clear();
        assertTrue(check.observedMask().get(8));
        assertSame(Thread.currentThread(), lock.assignedThread);
        assertTrue(lock.isBound());
    }
    @Test void broadAndWrongMasksAreMismatches() {
        BitSet mask = new BitSet(); mask.set(8); mask.set(9);
        assertEquals(BindingCheck.Status.MISMATCH, BindingCheck.inspect(lock(8, true), backend(mask)).status());
        mask.clear(8);
        assertEquals("mask_mismatch", BindingCheck.inspect(lock(8, true), backend(mask)).reason());
    }
    @Test void ownershipReservationAndAllocationAreDistinct() {
        AffinityLock lock = lock(8, false);
        assertEquals("reserved_not_bound", BindingCheck.inspect(lock, NullAffinity.INSTANCE).reason());
        lock.bound = true; lock.assignedThread = new Thread();
        assertEquals("not_current_thread_owner", BindingCheck.inspect(lock, NullAffinity.INSTANCE).reason());
        assertEquals("unallocated", BindingCheck.inspect(lock(-1, false), NullAffinity.INSTANCE).reason());
    }
    @Test void unsupportedAndUnavailableAreNeverVerified() {
        AffinityLock lock = lock(8, true);
        assertEquals(BindingCheck.Status.UNKNOWN, BindingCheck.inspect(lock, NullAffinity.INSTANCE).status());
        assertEquals(BindingCheck.Status.UNKNOWN, BindingCheck.inspect(lock, backend(null)).status());
        assertEquals(BindingCheck.Status.UNKNOWN, BindingCheck.inspect(lock, backend(new BitSet())).status());
    }
    @Test void threadDescriptionDoesNotChangeMask() {
        BitSet before = Affinity.getAffinity();
        assertTrue(Affinity.describeCurrentThread().contains("allowed_mask="));
        assertEquals(before, Affinity.getAffinity());
    }
    @Test void realPublicCheckDoesNotAcquireOrRelease() {
        LockInventory inventory = new LockInventory(new net.openhft.affinity.impl.NoCpuLayout(1));
        AffinityLock lock = new AffinityLock(0, 0, false, true, inventory);
        lock.assignedThread = Thread.currentThread();
        assertEquals("reserved_not_bound", lock.checkBound().reason());
        assertSame(Thread.currentThread(), lock.assignedThread);
    }
}
