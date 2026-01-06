/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BootClassPathTest extends BaseAffinitySupport {
    @Test
    public void shouldDetectClassesOnClassPath() {
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"), "java.lang.Thread on classpath");
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"), "java.lang.Runtime on classpath");
    }
}
