/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.lockchecker;

import java.io.IOException;

/**
 * @author Tom Shercliff
 */

public interface LockChecker {

    boolean isLockFree(int id);

    /**
     * Obtain a lock for the given id.
     */
    @Deprecated(/* to be removed in x.29 */)
    default boolean obtainLock(int id, String metaInfo) throws IOException {
        return obtainLock(id, 0, metaInfo);
    }

    /**
     * Obtain a lock for the given id and id2. The id2 is used to distinguish between
     * multiple locks for the same core
     */
    boolean obtainLock(int id, int id2, String metaInfo) throws IOException;

    boolean releaseLock(int id);

    String getMetaInfo(int id) throws IOException;
}
