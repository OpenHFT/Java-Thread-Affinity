/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * Allow you define a strategy for find the a cpu relative to another select cpu.
 *
 * @author peter.lawrey
 */
public interface AffinityStrategy {
    /**
     * @param cpuId  to cpuId to compare
     * @param cpuId2 with a second cpuId
     * @return true if it matches the criteria.
     */
    boolean matches(int cpuId, int cpuId2);
}
