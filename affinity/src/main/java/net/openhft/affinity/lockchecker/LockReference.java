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
