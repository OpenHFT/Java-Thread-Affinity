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

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * The LockReference class represents a reference to a file lock, holding both the file channel and the file lock.
 * It is used to manage locks on CPU cores.
 *
 * Author: Tom Shercliff
 */

public class LockReference {
    // The file channel associated with the lock
    protected final FileChannel channel;

    // The file lock
    protected final FileLock lock;

    /**
     * Constructs a LockReference with the specified file channel and file lock.
     *
     * @param channel the file channel associated with the lock
     * @param lock    the file lock
     */
    public LockReference(final FileChannel channel, final FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Returns the file channel associated with the lock.
     *
     * @return the file channel
     */
    public FileChannel getChannel() {
        return channel;
    }
}
