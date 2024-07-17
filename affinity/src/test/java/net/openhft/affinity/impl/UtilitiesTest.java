package net.openhft.affinity.impl;

import junit.framework.TestCase;

import java.util.BitSet;

public class UtilitiesTest extends TestCase {

    public void testToHexString() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(4);

        String hexString = Utilities.toHexString(bitSet);
        assertNotNull(hexString);
        assertEquals("13", hexString); // 1101 in binary is 13 in hexadecimal
    }

    public void testToBinaryString() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(4);

        String binaryString = Utilities.toBinaryString(bitSet);
        assertNotNull(binaryString);
        assertEquals("10011", binaryString); // Corrected to match little-endian order
    }

    public void testIs64Bit() {
        boolean is64Bit = Utilities.is64Bit();
        String systemProp = System.getProperty("os.arch");
        if (systemProp != null) {
            if (systemProp.contains("64")) {
                assertTrue(is64Bit);
            } else {
                assertFalse(is64Bit);
            }
        }
    }

    public void testIsLinux() {
        boolean isLinux = Utilities.ISLINUX;
        assertEquals("Linux".equals(System.getProperty("os.name")), isLinux);
    }
}
