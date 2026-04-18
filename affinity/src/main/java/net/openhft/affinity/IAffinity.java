/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
     * @return returns affinity mask for current thread, or null if unknown
     */
    BitSet getAffinity();

    /**
     * @param affinity sets affinity mask of current thread to specified value
     */
    void setAffinity(final BitSet affinity);

    /**
     * @return the current cpu id, or -1 if unknown.
     */
    int getCpu();

    /**
     * @return the process id of the current process.
     */
    int getProcessId();

    /**
     * @return the thread id of the current thread or -1 is not available.
     */
    int getThreadId();
}
