/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class AffinityJnaUnavailableSimulationTest extends BaseAffinitySupport {

    @Test
    public void whenJnaUnavailableFallsBackToNullAffinity() {
        assumeFalse(Affinity.isJNAAvailable(), "requires JNA to be unavailable");
        IAffinity impl = Affinity.getAffinityImpl();
        assertInstanceOf(NullAffinity.class, impl, "expected NullAffinity when JNA is not available");
    }
}
