/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class AffinitySelectionAndFallbackTest extends BaseAffinityTest {

    @Test
    public void defaultsToLinuxJnaImplementationOnLinux() {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"));
        assumeTrue("JNA must be available for this test", Affinity.isJNAAvailable());
        assumeTrue("LinuxJNAAffinity must be loaded", LinuxJNAAffinity.LOADED);

        IAffinity impl = Affinity.getAffinityImpl();
        assertTrue("Expected LinuxJNAAffinity as default implementation on Linux",
                impl instanceof LinuxJNAAffinity);
    }

    @Test
    public void fallsBackToNullAffinityWhenJnaUnavailable() {
        // This behaviour can only be asserted when JNA is genuinely unavailable
        // on the classpath. When JNA is present, we skip the assertion.
        if (Affinity.isJNAAvailable()) {
            return;
        }
        IAffinity impl = Affinity.getAffinityImpl();
        assertTrue("Expected NullAffinity when JNA is not available",
                impl instanceof net.openhft.affinity.impl.NullAffinity);
    }
}

