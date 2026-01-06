/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilitiesTest extends BaseAffinitySupport {

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
        assertEquals("", hex(set), "hex: empty");
        assertEquals("1", hex(set, 0), "hex: bit0");
        assertEquals("10", hex(set, 4), "hex: bit4");
        assertEquals(Long.toHexString(1L << 63), hex(set, 63), "hex: bit63");
        assertEquals("01", hex(set, 64), "hex: bit64");
        assertEquals("101", hex(set, 0, 128), "hex: bit0+bit128");
    }

    @Test
    public void testToBinaryString() {
        BitSet set = new BitSet();
        assertEquals("", bin(set), "binary: empty");
        assertEquals("1", bin(set, 0), "binary: bit0");
        assertEquals("10000", bin(set, 4), "binary: bit4");
        assertEquals(Long.toBinaryString(1L << 63), bin(set, 63), "binary: bit63");
        assertEquals("01", bin(set, 64), "binary: bit64");
        assertEquals("101", bin(set, 0, 128), "binary: bit0+bit128");
    }
}
