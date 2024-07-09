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

package net.openhft.affinity;

/**
 * The CpuLayout interface provides methods to retrieve information about the CPU layout,
 * including the number of CPUs, sockets, cores per socket, and threads per core.
 * It also allows for determining the socket, core, and thread ID for a given logical processor number.
 */
public interface CpuLayout {

    /**
     * Returns the number of CPUs.
     *
     * @return the number of CPUs
     */
    int cpus();

    /**
     * Returns the number of sockets.
     *
     * @return the number of sockets
     */
    int sockets();

    /**
     * Returns the number of cores per socket.
     *
     * @return the number of cores per socket
     */
    int coresPerSocket();

    /**
     * Returns the number of threads per core.
     *
     * @return the number of threads per core
     */
    int threadsPerCore();

    /**
     * Returns the socket ID for the given logical processor number.
     *
     * @param cpuId the logical processor number
     * @return which socket id this cpu is on.
     */
    int socketId(int cpuId);

    /**
     * Returns the core ID on a socket for the given logical processor number.
     *
     * @param cpuId the logical processor number
     * @return which core on a socket this cpu is on.
     */
    int coreId(int cpuId);

    /**
     * Returns the thread ID on a core for the given logical processor number.
     *
     * @param cpuId the logical processor number
     * @return which thread on a core this cpu is on.
     */
    int threadId(int cpuId);
}
