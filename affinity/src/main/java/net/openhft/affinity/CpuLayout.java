/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * Describes the physical CPU topology exposed to the JVM.
 */
public interface CpuLayout {
    /**
     * Returns the number of logical CPUs available.
     *
     * @return the number of cpus.
     */
    int cpus();

    /**
     * Returns how many sockets the machine exposes.
     *
     * @return number of sockets available
     */
    int sockets();

    /**
     * Returns the number of cores on each socket.
     *
     * @return number of cores per socket
     */
    int coresPerSocket();

    /**
     * Returns the number of hardware threads per core.
     *
     * @return number of hardware threads per core
     */
    int threadsPerCore();

    /**
     * Identifies the socket for a logical CPU id.
     *
     * @param cpuId the logical processor number
     * @return which socket id this cpu is on.
     */
    int socketId(int cpuId);

    /**
     * Identifies the core within a socket for a logical CPU id.
     *
     * @param cpuId the logical processor number
     * @return which core on a socket this cpu is on.
     */
    int coreId(int cpuId);

    /**
     * Identifies the hardware thread within a core for a logical CPU id.
     *
     * @param cpuId the logical processor number
     * @return which thread on a core this cpu is on.
     */
    int threadId(int cpuId);

    /**
     * Returns the hyperthreaded pair number for a logical CPU id.
     *
     * @param cpuId the logical processor number
     * @return the hyperthreaded pair number or 0 if not hyperthreaded.
     */
    int pair(int cpuId);
}
