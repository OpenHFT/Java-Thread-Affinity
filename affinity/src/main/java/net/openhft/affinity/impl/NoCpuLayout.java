/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
