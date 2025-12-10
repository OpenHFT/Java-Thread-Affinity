/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.lockchecker;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Holds the file channel and lock associated with an acquired affinity marker.
 */
public class LockReference {
    /**
     * Channel backing the on-disk lock file.
     */
    protected final FileChannel channel;
    /**
     * Platform file lock guarding the affinity slot.
     */
    protected final FileLock lock;

    /**
     * Creates a reference wrapper around an open lock file.
     *
     * @param channel channel pointing at the lock file
     * @param lock    acquired file lock
     */
    public LockReference(final FileChannel channel, final FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Returns the channel backing the lock file.
     *
     * @return channel used for locking
     */
    public FileChannel getChannel() {
        return channel;
    }
}
