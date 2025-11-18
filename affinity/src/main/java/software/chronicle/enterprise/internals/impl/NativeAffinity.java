/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals.impl;

import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

public enum NativeAffinity implements IAffinity {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeAffinity.class);

    public static final boolean LOADED;
    public static final String VERSION;

    static {
        LOADED = loadAffinityNativeLibrary();
        if (LOADED) {
            VERSION = getVersion0();
            LOGGER.info("Loaded Chronicle Affinity native library version {}", VERSION);
        } else {
            VERSION = "not loaded";
        }
    }

    private static native String getVersion0();

    private static native byte[] getAffinity0();

    private static native void setAffinity0(byte[] affinity);

    private static native int getCpu0();

    private static native int getProcessId0();

    private static native int getThreadId0();

    private static native long rdtsc0();

    static long rdtsc() {
        return rdtsc0();
    }

    @SuppressWarnings("restricted")
    private static boolean loadAffinityNativeLibrary() {
        try {
            System.loadLibrary("CEInternals");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public BitSet getAffinity() {
        final byte[] buff = getAffinity0();
        if (buff == null) {
            return null;
        }
        return BitSet.valueOf(buff);
    }

    @Override
    public void setAffinity(BitSet affinity) {
        setAffinity0(affinity.toByteArray());
    }

    @Override
    public int getCpu() {
        return getCpu0();
    }

    @Override
    public int getProcessId() {
        return getProcessId0();
    }

    @Override
    public int getThreadId() {
        return getThreadId0();
    }
}
