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

package net.openhft.affinity;

/**
 * Pre-defined strategies for determining which thread to pick next.
 *
 * @author peter.lawrey
 */
public enum AffinityStrategies implements AffinityStrategy {
    /**
     * Any free cpu.
     */
    ANY {
        @Override
        public boolean matches(int cpuId, int cpuId2) {
            return true;
        }
    },
    /**
     * Must be a cpu on the same core.
     */
    SAME_CORE {
        @Override
        public boolean matches(int cpuId, int cpuId2) {
            CpuLayout cpuLayout = AffinityLock.cpuLayout();
            return cpuLayout.socketId(cpuId) == cpuLayout.socketId(cpuId2) &&
                    cpuLayout.coreId(cpuId) == cpuLayout.coreId(cpuId2);
        }
    },
    /**
     * Must be a cpu on the same socket/chip but different core.
     */
    SAME_SOCKET {
        @Override
        public boolean matches(int cpuId, int cpuId2) {
            CpuLayout cpuLayout = AffinityLock.cpuLayout();
            return cpuLayout.socketId(cpuId) == cpuLayout.socketId(cpuId2) &&
                    cpuLayout.coreId(cpuId) != cpuLayout.coreId(cpuId2);
        }
    },
    /**
     * Must be a cpu on any other core (or socket)
     */
    DIFFERENT_CORE {
        @Override
        public boolean matches(int cpuId, int cpuId2) {
            CpuLayout cpuLayout = AffinityLock.cpuLayout();
            return cpuLayout.socketId(cpuId) != cpuLayout.socketId(cpuId2) ||
                    cpuLayout.coreId(cpuId) != cpuLayout.coreId(cpuId2);
        }
    },
    /**
     * Must be a cpu on any other socket.
     */
    DIFFERENT_SOCKET {
        @Override
        public boolean matches(int cpuId, int cpuId2) {
            CpuLayout cpuLayout = AffinityLock.cpuLayout();
            return cpuLayout.socketId(cpuId) != cpuLayout.socketId(cpuId2);
        }
    }
}
