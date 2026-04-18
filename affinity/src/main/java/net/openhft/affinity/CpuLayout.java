/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * @author peter.lawrey
 */
public interface CpuLayout {
    /**
     * @return the number of cpus.
     */
    int cpus();

    int sockets();

    int coresPerSocket();

    int threadsPerCore();

    /**
     * @param cpuId the logical processor number
     * @return which socket id this cpu is on.
     */
    int socketId(int cpuId);

    /**
     * @param cpuId the logical processor number
     * @return which core on a socket this cpu is on.
     */
    int coreId(int cpuId);

    /**
     * @param cpuId the logical processor number
     * @return which thread on a core this cpu is on.
     */
    int threadId(int cpuId);

    /**
     * @param cpuId the logical processor number
     * @return the hyperthreaded pair number or 0 if not hyperthreaded.
     */
    int pair(int cpuId);
}
