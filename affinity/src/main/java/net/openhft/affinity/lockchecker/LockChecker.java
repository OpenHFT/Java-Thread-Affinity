/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.lockchecker;

import java.io.IOException;

/**
 * The LockChecker interface defines methods for checking, obtaining, and releasing locks on CPU cores.
 * Implementations of this interface provide mechanisms to ensure that only one process can lock a particular CPU core at a time.
 *
 * Author: Tom Shercliff
 */
public interface LockChecker {

    /**
     * Checks if a lock is free for a given CPU ID.
     *
     * @param id the CPU ID to check
     * @return true if the lock is free, false otherwise
     */
    boolean isLockFree(int id);

    /**
     * Attempts to obtain a lock for a given CPU ID with meta-information.
     *
     * @param id       the CPU ID to lock
     * @param metaInfo the meta-information to write to the lock file
     * @return true if the lock was successfully obtained, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean obtainLock(int id, String metaInfo) throws IOException;

    /**
     * Releases the lock for a given CPU ID.
     *
     * @param id the CPU ID to release the lock for
     * @return true if the lock was successfully released, false otherwise
     */
    boolean releaseLock(int id);

    /**
     * Retrieves the meta-information for a given CPU ID.
     *
     * @param id the CPU ID to get the meta-information for
     * @return the meta-information string, or null if not found
     * @throws IOException if an I/O error occurs
     */
    String getMetaInfo(int id) throws IOException;
}
