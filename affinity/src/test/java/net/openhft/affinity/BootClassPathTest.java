//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.affinity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BootClassPathTest extends BaseAffinityTest {
    @Test
    public void shouldDetectClassesOnClassPath() {
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}
