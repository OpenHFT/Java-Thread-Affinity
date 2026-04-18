/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.CpuLayout;

/**
 * This assumes there is one socket with every cpu on a different core.
 *
 * @author peter.lawrey
 */
public class NoCpuLayout implements CpuLayout {
    private final int cpus;

    public NoCpuLayout(int cpus) {
        this.cpus = cpus;
    }

    @Override
    public int sockets() {
        return 1;
    }

    @Override
    public int coresPerSocket() {
        return cpus;
    }

    @Override
    public int threadsPerCore() {
        return 1;
    }

    public int cpus() {
        return cpus;
    }

    @Override
    public int socketId(int cpuId) {
        return 0;
    }

    @Override
    public int coreId(int cpuId) {
        return cpuId;
    }

    @Override
    public int threadId(int cpuId) {
        return 0;
    }

    @Override
    public int pair(int cpuId) {
        return 0;
    }
}
