/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link VanillaCpuLayout#pair(int)} using sample cpuinfo files.
 */
public class VanillaCpuLayoutPairTest extends BaseAffinitySupport {

    @Test
    public void testPairForI7() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i7.cpuinfo")) {
            assertNotNull(is, "test resource: i7.cpuinfo");
            VanillaCpuLayout layout = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(4, layout.pair(0), "i7 pair(0)");
            assertEquals(5, layout.pair(1), "i7 pair(1)");
            assertEquals(6, layout.pair(2), "i7 pair(2)");
            assertEquals(7, layout.pair(3), "i7 pair(3)");
            assertEquals(0, layout.pair(4), "i7 pair(4)");
            assertEquals(1, layout.pair(5), "i7 pair(5)");
            assertEquals(2, layout.pair(6), "i7 pair(6)");
            assertEquals(3, layout.pair(7), "i7 pair(7)");
        }
    }

    @Test
    public void testPairForI3() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i3.cpuinfo")) {
            assertNotNull(is, "test resource: i3.cpuinfo");
            VanillaCpuLayout layout = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(2, layout.pair(0), "i3 pair(0)");
            assertEquals(3, layout.pair(1), "i3 pair(1)");
            assertEquals(0, layout.pair(2), "i3 pair(2)");
            assertEquals(1, layout.pair(3), "i3 pair(3)");
        }
    }
}
