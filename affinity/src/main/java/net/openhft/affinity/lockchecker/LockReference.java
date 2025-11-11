/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.lockchecker;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * @author Tom Shercliff
 */

public class LockReference {
    protected final FileChannel channel;
    protected final FileLock lock;

    public LockReference(final FileChannel channel, final FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public FileChannel getChannel() {
        return channel;
    }
}
