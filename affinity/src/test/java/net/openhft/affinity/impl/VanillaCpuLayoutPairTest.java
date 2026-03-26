/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link VanillaCpuLayout#pair(int)} using sample cpuinfo files.
 */
class VanillaCpuLayoutPairTest extends BaseAffinityTest {

    @Test
    void testPairForI7() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i7.cpuinfo")) {
            VanillaCpuLayout layout = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(4, layout.pair(0));
            assertEquals(5, layout.pair(1));
            assertEquals(6, layout.pair(2));
            assertEquals(7, layout.pair(3));
            assertEquals(0, layout.pair(4));
            assertEquals(1, layout.pair(5));
            assertEquals(2, layout.pair(6));
            assertEquals(3, layout.pair(7));
        }
    }

    @Test
    void testPairForI3() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i3.cpuinfo")) {
            VanillaCpuLayout layout = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(2, layout.pair(0));
            assertEquals(3, layout.pair(1));
            assertEquals(0, layout.pair(2));
            assertEquals(1, layout.pair(3));
        }
    }
}
