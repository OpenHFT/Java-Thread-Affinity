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

import net.openhft.affinity.CpuLayout;

/**
 * The NoCpuLayout class provides a simple implementation of the CpuLayout interface,
 * assuming there is one socket with every CPU on a different core.
 * <p>
 * Author: Peter Lawrey
 */
public class NoCpuLayout implements CpuLayout {
    // Number of CPUs in the system
    private final int cpus;

    /**
     * Constructs a NoCpuLayout with the specified number of CPUs.
     *
     * @param cpus the number of CPUs
     */
    public NoCpuLayout(int cpus) {
        this.cpus = cpus;
    }

    /**
     * Returns the number of sockets in the system.
     *
     * @return the number of sockets, which is always 1 in this implementation
     */
    @Override
    public int sockets() {
        return 1;
    }

    /**
     * Returns the number of cores per socket.
     *
     * @return the number of cores per socket, which is equal to the number of CPUs in this implementation
     */
    @Override
    public int coresPerSocket() {
        return cpus;
    }

    /**
     * Returns the number of threads per core.
     *
     * @return the number of threads per core, which is always 1 in this implementation
     */
    @Override
    public int threadsPerCore() {
        return 1;
    }

    /**
     * Returns the number of CPUs in the system.
     *
     * @return the number of CPUs
     */
    @Override
    public int cpus() {
        return cpus;
    }

    /**
     * Returns the socket ID for a given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the socket ID, which is always 0 in this implementation
     */
    @Override
    public int socketId(int cpuId) {
        return 0;
    }

    /**
     * Returns the core ID for a given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the core ID, which is equal to the CPU ID in this implementation
     */
    @Override
    public int coreId(int cpuId) {
        return cpuId;
    }

    /**
     * Returns the thread ID for a given CPU ID.
     *
     * @param cpuId the CPU ID
     * @return the thread ID, which is always 0 in this implementation
     */
    @Override
    public int threadId(int cpuId) {
        return 0;
    }
}
