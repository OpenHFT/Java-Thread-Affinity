/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import org.slf4j.Logger;

import java.util.BitSet;
import java.util.function.IntSupplier;

final class NoAffinitySupport {
    private NoAffinitySupport() {
    }

    static BitSet emptyAffinity() {
        return new BitSet();
    }

    static void logUnableToSet(Logger logger, BitSet affinity) {
        logger.trace("unable to set mask to {} as the JNI and JNA libraries not loaded", Utilities.toHexString(affinity));
    }

    static int cpuUnsupported() {
        return -1;
    }

    static int currentProcessId() {
        return Utilities.currentProcessId();
    }

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
