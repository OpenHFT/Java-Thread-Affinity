/*
 * Copyright 2016-2025 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.impl;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class UtilitiesTest {

    private static String hex(BitSet set, int... bits) {
        set.clear();
        for (int b : bits) {
            set.set(b);
        }
        return Utilities.toHexString(set);
    }

    private static String bin(BitSet set, int... bits) {
        set.clear();
        for (int b : bits) {
            set.set(b);
        }
        return Utilities.toBinaryString(set);
    }

    @Test
    public void testToHexString() {
        BitSet set = new BitSet();
        assertEquals("", hex(set));
        assertEquals("1", hex(set, 0));
        assertEquals("10", hex(set, 4));
        assertEquals(Long.toHexString(1L << 63), hex(set, 63));
        assertEquals("01", hex(set, 64));
        assertEquals("101", hex(set, 0, 128));
    }

    @Test
    public void testToBinaryString() {
        BitSet set = new BitSet();
        assertEquals("", bin(set));
        assertEquals("1", bin(set, 0));
        assertEquals("10000", bin(set, 4));
        assertEquals(Long.toBinaryString(1L << 63), bin(set, 63));
        assertEquals("01", bin(set, 64));
        assertEquals("101", bin(set, 0, 128));
    }
}
