/*
 * Copyright 2016-2020 chronicle.software
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
 *
 */

package net.openhft.affinity.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.BitSet;

/**
 * Utility class providing various static methods for handling bit sets and system properties.
 * <p>
 * This class cannot be instantiated.
 */
public final class Utilities {

    // Constant indicating whether the operating system is Linux
    public static final boolean ISLINUX = "Linux".equals(System.getProperty("os.name"));

    // Constant indicating whether the JVM is running in 64-bit mode
    static final boolean IS64BIT = is64Bit0();

    // Private constructor to prevent instantiation
    private Utilities() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Creates a hexademical representation of the bit set
     *
     * @param set the BitSet to convert
     * @return the hexadecimal string representation of the BitSet
     */
    public static String toHexString(final BitSet set) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        final long[] longs = set.toLongArray();
        for (long aLong : longs) {
            writer.write(Long.toHexString(aLong));
        }
        writer.flush();

        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Creates a binary representation of the given BitSet.
     *
     * @param set the BitSet to convert
     * @return the binary string representation of the BitSet
     */
    public static String toBinaryString(BitSet set) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        final long[] longs = set.toLongArray();
        for (long aLong : longs) {
            writer.write(Long.toBinaryString(aLong));
        }
        writer.flush();

        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Checks if the JVM is running in 64-bit mode.
     *
     * @return true if the JVM is 64-bit, false otherwise
     */
    public static boolean is64Bit() {
        return IS64BIT;
    }

    /**
     * Determines if the JVM is running in 64-bit mode by checking system properties.
     *
     * @return true if the JVM is 64-bit, false otherwise
     */
    private static boolean is64Bit0() {
        String systemProp;
        systemProp = System.getProperty("com.ibm.vm.bitmode");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("sun.arch.data.model");
        if (systemProp != null) {
            return "64".equals(systemProp);
        }
        systemProp = System.getProperty("java.vm.version");
        return systemProp != null && systemProp.contains("_64");
    }
}
