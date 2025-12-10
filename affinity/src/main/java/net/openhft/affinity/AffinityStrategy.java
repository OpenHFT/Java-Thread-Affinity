/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * Strategy interface for choosing CPUs relative to an existing allocation.
 */
public interface AffinityStrategy {
    /**
     * Determines whether a candidate CPU pair matches the strategy.
     *
     * @param cpuId  to cpuId to compare
     * @param cpuId2 with a second cpuId
     * @return true if it matches the criteria.
     */
    boolean matches(int cpuId, int cpuId2);
}
