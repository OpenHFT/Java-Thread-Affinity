/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.lockchecker;

import java.io.IOException;

/**
 * Abstraction for reserving CPU ids via lightweight lock files.
 */
public interface LockChecker {

    /**
     * Checks if a particular logical CPU id is currently unreserved.
     *
     * @param id logical CPU index
     * @return {@code true} if no reservation exists
     */
    boolean isLockFree(int id);

    /**
     * Obtain a lock for the given id.
     *
     * @param id       logical CPU index
     * @param metaInfo metadata to record alongside the lock
     * @return {@code true} if the lock was acquired
     * @throws IOException if the lock file cannot be written
     */
    @Deprecated(/* to be removed in x.29 */)
    default boolean obtainLock(int id, String metaInfo) throws IOException {
        return obtainLock(id, 0, metaInfo);
    }

    /**
     * Obtain a lock for the given id and id2. The id2 is used to distinguish between
     * multiple locks for the same core
     *
     * @param id       primary logical CPU index
     * @param id2      secondary logical CPU index to disambiguate hyper-threads
     * @param metaInfo metadata to record alongside the lock
     * @return {@code true} if the lock was acquired
     * @throws IOException if the lock file cannot be written
     */
    boolean obtainLock(int id, int id2, String metaInfo) throws IOException;

    /**
     * Releases any lock held for the given logical CPU id.
     *
     * @param id logical CPU index
     * @return {@code true} if a lock was cleared
     */
    boolean releaseLock(int id);

    /**
     * Returns metadata recorded with a lock for the given CPU id.
     *
     * @param id logical CPU index
     * @return metadata string or {@code null} if none
     * @throws IOException if the lock file cannot be read
     */
    String getMetaInfo(int id) throws IOException;
}
