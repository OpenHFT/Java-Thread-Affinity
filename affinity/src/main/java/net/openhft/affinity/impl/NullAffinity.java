/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.BitSet;

/**
 * The NullAffinity class provides a fallback implementation of the IAffinity interface
 * that performs no operations. This is used when JNI and JNA libraries are not loaded.
 * <p>
 * Author: Peter Lawrey
 * </p>
 */
public enum NullAffinity implements IAffinity {
    INSTANCE;

    // Logger instance for logging messages
    private static final Logger LOGGER = LoggerFactory.getLogger(NullAffinity.class);

    /**
     * Gets the CPU affinity of the current process. This implementation returns an empty BitSet.
     *
     * @return an empty BitSet
     */
    @Override
    public BitSet getAffinity() {
        return new BitSet();
    }

    /**
     * Sets the CPU affinity for the current process. This implementation logs a message and performs no action.
     *
     * @param affinity the BitSet representing the CPU affinity
     */
    @Override
    public void setAffinity(final BitSet affinity) {
        LOGGER.trace("Unable to set mask to {} as the JNI and JNA libraries are not loaded", Utilities.toHexString(affinity));
    }

    /**
     * Gets the current CPU that the calling thread is running on. This implementation returns -1.
     *
     * @return -1 indicating that the operation is not supported
     */
    @Override
    public int getCpu() {
        return -1;
    }

    /**
     * Gets the process ID of the current process.
     *
     * @return the process ID
     */
    @Override
    public int getProcessId() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    /**
     * Gets the thread ID of the calling thread. This implementation throws UnsupportedOperationException.
     *
     * @return never returns as it always throws UnsupportedOperationException
     */
    @Override
    public int getThreadId() {
        throw new UnsupportedOperationException();
    }
}
