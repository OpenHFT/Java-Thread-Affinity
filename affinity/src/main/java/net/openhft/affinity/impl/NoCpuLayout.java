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
}
