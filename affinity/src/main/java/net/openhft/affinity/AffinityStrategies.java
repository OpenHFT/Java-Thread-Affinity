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
