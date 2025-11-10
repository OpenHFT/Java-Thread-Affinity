//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//
package net.openhft.affinity.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.BitSet;

/*
 * Created by andre on 20/06/15.
 */
public final class Utilities {
    public static final boolean ISLINUX = "Linux".equals(System.getProperty("os.name"));
    static final boolean IS64BIT = is64Bit0();

    private Utilities() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Creates a hexademical representation of the bit set
     *
     * @param set the bit set to convert
     * @return the hexademical string representation
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

    public static boolean is64Bit() {
        return IS64BIT;
    }

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

    /**
     * Returns the current process id. Uses {@code ProcessHandle} when running
     * on Java&nbsp;9 or later and falls back to parsing
     * {@code RuntimeMXBean#getName()} on earlier versions.
     *
     * @return the process id or {@code -1} if it cannot be determined
     */
    public static int currentProcessId() {
        try {
            // Java 9+ provides ProcessHandle which has a pid() method.
            Class<?> phClass = Class.forName("java.lang.ProcessHandle");
            Object current = phClass.getMethod("current").invoke(null);
            long pid = (Long) phClass.getMethod("pid").invoke(current);
            return (int) pid;
        } catch (Throwable ignored) {
            // ignore and fallback to the pre-Java 9 approach
        }

        try {
            String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            return Integer.parseInt(name.split("@")[0]);
        } catch (Throwable e) {
            return -1;
        }
    }
}
