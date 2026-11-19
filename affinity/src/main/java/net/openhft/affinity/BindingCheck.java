/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import java.util.BitSet;

/** Point-in-time current-thread verification. Never binds, releases or throws on a mismatch. */
public final class BindingCheck {
    public enum Status { VERIFIED, MISMATCH, UNKNOWN }
    private final Status status;
    private final String reason;
    private final BitSet mask;

    private BindingCheck(Status status, String reason, BitSet mask) {
        this.status = status;
        this.reason = reason;
        this.mask = mask == null ? null : (BitSet) mask.clone();
    }
    public Status status() { return status; }
    public String reason() { return reason; }
    public BitSet observedMask() { return mask == null ? null : (BitSet) mask.clone(); }
    @Override public String toString() { return status + ": " + reason + "; mask=" + mask; }

    static BindingCheck inspect(AffinityLock lock, IAffinity backend) {
        if (!lock.isAllocated()) return result(Status.MISMATCH, "unallocated", null);
        if (lock.assignedThread != Thread.currentThread())
            return result(Status.MISMATCH, "not_current_thread_owner", null);
        if (!lock.isBound()) return result(Status.MISMATCH, "reserved_not_bound", null);
        if (backend instanceof NullAffinity) return result(Status.UNKNOWN, "unsupported_backend", null);
        try {
            BitSet mask = backend.getAffinity();
            if (mask == null || mask.isEmpty()) return result(Status.UNKNOWN, "mask_unavailable", mask);
            boolean singleton = mask.cardinality() == 1 && mask.get(lock.cpuId());
            return result(singleton ? Status.VERIFIED : Status.MISMATCH,
                    singleton ? "singleton_mask_matches_reservation" : "mask_mismatch", mask);
        } catch (RuntimeException | LinkageError unavailable) {
            return result(Status.UNKNOWN, "mask_unavailable:" + unavailable.getClass().getSimpleName(), null);
        }
    }
    private static BindingCheck result(Status status, String reason, BitSet mask) {
        return new BindingCheck(status, reason, mask);
    }
}
