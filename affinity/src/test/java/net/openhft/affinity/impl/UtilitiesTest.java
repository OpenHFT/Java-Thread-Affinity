/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class UtilitiesTest extends BaseAffinityTest {

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
