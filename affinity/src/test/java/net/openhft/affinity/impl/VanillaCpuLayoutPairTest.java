//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2016-2025 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link VanillaCpuLayout#pair(int)} using sample cpuinfo files.
 */
public class VanillaCpuLayoutPairTest extends BaseAffinityTest {

    @Test
    public void testPairForI7() throws IOException {
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
    public void testPairForI3() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("i3.cpuinfo")) {
            VanillaCpuLayout layout = VanillaCpuLayout.fromCpuInfo(is);
            assertEquals(2, layout.pair(0));
            assertEquals(3, layout.pair(1));
            assertEquals(0, layout.pair(2));
            assertEquals(1, layout.pair(3));
        }
    }
}
