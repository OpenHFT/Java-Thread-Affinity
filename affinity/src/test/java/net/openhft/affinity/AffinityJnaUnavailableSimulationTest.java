/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AffinityJnaUnavailableSimulationTest extends BaseAffinityTest {

    @Test
    public void whenJnaUnavailableFallsBackToNullAffinity() {
        // This test only asserts behaviour when JNA is genuinely unavailable
        // in the runtime. When JNA is present, the test is effectively a no-op.
        if (Affinity.isJNAAvailable()) {
            return;
        }
        IAffinity impl = Affinity.getAffinityImpl();
        assertTrue("Expected NullAffinity when JNA is not available",
                impl instanceof NullAffinity);
    }
}
