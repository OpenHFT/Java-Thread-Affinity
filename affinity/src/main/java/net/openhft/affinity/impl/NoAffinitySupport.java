/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import org.slf4j.Logger;

import java.util.BitSet;
import java.util.function.IntSupplier;

/**
 * Shared helpers for {@link NoAffinity} implementations where affinity control is unavailable.
 * These methods avoid native calls and use lightweight fallbacks so callers can safely proceed.
 */
final class NoAffinitySupport {
    /**
     * Utility holder; not instantiable.
     */
    private NoAffinitySupport() {
    }

    /**
     * Returns an empty affinity mask to signal "no binding".
     */
    static BitSet emptyAffinity() {
        return new BitSet();
    }

    /**
     * Emits a trace message explaining that affinity could not be applied; used instead of
     * throwing so the caller can degrade gracefully.
     */
    static void logUnableToSet(Logger logger, BitSet affinity) {
        logger.trace("unable to set mask to {} as the JNI and JNA libraries not loaded", Utilities.toHexString(affinity));
    }

    /**
     * Sentinel value returned when CPU detection is unsupported.
     */
    static int cpuUnsupported() {
        return -1;
    }

    /**
     * Lightweight process id lookup that does not rely on native affinity libraries.
     */
    static int currentProcessId() {
        return Utilities.currentProcessId();
    }

    /**
     * Retrieves or computes a thread id using the supplied strategy, masking it to the historical
     * unsigned 24-bit range expected by legacy code.
     */
    static int threadId(ThreadLocal<Integer> threadId, IntSupplier supplier) {
        Integer tid = threadId.get();
        if (tid == null) {
            tid = supplier.getAsInt();
            //The tid assumed to be an unsigned 24 bit, see net.openhft.lang.Jvm.getMaxPid()
            tid = tid & 0xFFFFFF;
            threadId.set(tid);
        }
        return tid;
    }
}
