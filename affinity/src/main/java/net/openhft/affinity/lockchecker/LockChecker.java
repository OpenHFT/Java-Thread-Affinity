/*
 * Copyright 2016-2025 chronicle.software
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
