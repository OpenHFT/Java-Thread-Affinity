/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;

import java.util.BitSet;
import java.util.function.IntSupplier;

interface NoAffinity extends IAffinity {
    Logger logger();

    ThreadLocal<Integer> threadIdCache();

    IntSupplier threadIdSupplier();

    @Override
    default BitSet getAffinity() {
        return NoAffinitySupport.emptyAffinity();
    }

    @Override
    default void setAffinity(BitSet affinity) {
        NoAffinitySupport.logUnableToSet(logger(), affinity);
    }

    @Override
    default int getCpu() {
        return NoAffinitySupport.cpuUnsupported();
    }

    @Override
    default int getProcessId() {
        return NoAffinitySupport.currentProcessId();
    }

    @Override
    default int getThreadId() {
        return NoAffinitySupport.threadId(threadIdCache(), threadIdSupplier());
    }
}
