/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.BitSet;

/**
 * Implementation interface
 *
 * @author cheremin
 * @since 29.12.11,  20:14
 */
public interface IAffinity {
    /**
     * Obtains the affinity mask for the current thread.
     *
     * @return affinity mask, or null if unknown
     */
    BitSet getAffinity();

    /**
     * Applies the provided affinity mask to the current thread.
     *
     * @param affinity desired mask
     */
    void setAffinity(final BitSet affinity);

    /**
     * Returns the logical CPU of the current thread.
     *
     * @return cpu id, or -1 if unknown
     */
    int getCpu();

    /**
     * Returns the current process id.
     *
     * @return process id of the JVM
     */
    int getProcessId();

    /**
     * Returns the thread id of the current thread.
     *
     * @return thread id or -1 if unavailable
     */
    int getThreadId();
}
