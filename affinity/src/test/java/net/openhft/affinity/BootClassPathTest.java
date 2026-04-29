/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BootClassPathTest extends BaseAffinityTest {
    @Test
    void shouldDetectClassesOnClassPath() {
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}
