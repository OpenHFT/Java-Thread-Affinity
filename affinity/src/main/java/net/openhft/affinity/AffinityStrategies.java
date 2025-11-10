//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
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
