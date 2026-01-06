/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.LinuxJNAAffinity;
import net.openhft.affinity.impl.NullAffinity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AffinitySelectionAndFallbackTest extends BaseAffinitySupport {

    @Test
    public void defaultsToLinuxJnaImplementationOnLinux() {
        assumeTrue(System.getProperty("os.name").startsWith("Linux"), "requires Linux");
        assumeTrue(Affinity.isJNAAvailable(), "requires JNA");
        assumeTrue(LinuxJNAAffinity.LOADED, "requires LinuxJNAAffinity to be loaded");

        IAffinity impl = Affinity.getAffinityImpl();
        assertInstanceOf(LinuxJNAAffinity.class, impl, "default affinity implementation on Linux");
    }

    @Test
    public void fallsBackToNullAffinityWhenJnaUnavailable() {
        assumeFalse(Affinity.isJNAAvailable(), "requires JNA to be unavailable");
        IAffinity impl = Affinity.getAffinityImpl();
        assertInstanceOf(NullAffinity.class, impl, "expected NullAffinity when JNA is not available");
    }
}
