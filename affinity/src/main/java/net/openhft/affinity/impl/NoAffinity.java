/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;

import java.util.BitSet;
import java.util.function.IntSupplier;

/**
 * Fallback {@link IAffinity} implementation used when native/JNA bindings are not available.
 * All operations return sentinel values and emit trace logging rather than throwing so that
 * callers can continue with degraded behaviour.
 */
interface NoAffinity extends IAffinity {
    Logger logger();

    ThreadLocal<Integer> threadIdCache();

    IntSupplier threadIdSupplier();

    /**
     * Always returns an empty mask because no affinity controls are available.
     */
    @Override
    default BitSet getAffinity() {
        return NoAffinitySupport.emptyAffinity();
    }

    /**
     * Logs that the request was ignored because affinity cannot be set on this platform/config.
     */
    @Override
    default void setAffinity(BitSet affinity) {
        NoAffinitySupport.logUnableToSet(logger(), affinity);
    }

    /**
     * Indicates that CPU detection is unsupported when the native layer is missing.
     */
    @Override
    default int getCpu() {
        return NoAffinitySupport.cpuUnsupported();
    }

    /**
     * Delegates to the lightweight process id helper even in the no-affinity path.
     */
    @Override
    default int getProcessId() {
        return NoAffinitySupport.currentProcessId();
    }

    /**
     * Returns a cached thread id (or computes one) without consulting native APIs.
     */
    @Override
    default int getThreadId() {
        return NoAffinitySupport.threadId(threadIdCache(), threadIdSupplier());
    }
}
