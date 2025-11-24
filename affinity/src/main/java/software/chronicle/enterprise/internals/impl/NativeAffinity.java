/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals.impl;

import net.openhft.affinity.IAffinity;

import java.util.BitSet;

/**
 * Enterprise {@link IAffinity} backed by the native CEInternals library.
 * <p>
 * Provides affinity operations and lightweight cycle timing via JNI; guarded by the {@link #LOADED}
 * flag so callers can detect when the native library is unavailable.
 */
public enum NativeAffinity implements IAffinity {
    INSTANCE;

    /**
     * Indicates whether the native library loaded successfully.
     */
    public static final boolean LOADED;

    static {
        LOADED = loadAffinityNativeLibrary();
    }

    private static native byte[] getAffinity0();

    private static native void setAffinity0(byte[] affinity);

    private static native int getCpu0();

    private static native int getProcessId0();

    private static native int getThreadId0();

    private static native long rdtsc0();

    /**
     * Read the current cycle counter if the native library supports it.
     */
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

    /**
     * Read the affinity mask for the current thread.
     */
    @Override
    public BitSet getAffinity() {
        final byte[] buff = getAffinity0();
        if (buff == null) {
            return null;
        }
        return BitSet.valueOf(buff);
    }

    /**
     * Apply the given affinity mask to the current thread.
     */
    @Override
    public void setAffinity(BitSet affinity) {
        setAffinity0(affinity.toByteArray());
    }

    /**
     * Return the CPU id the current thread is running on.
     */
    @Override
    public int getCpu() {
        return getCpu0();
    }

    /**
     * Return the current process id.
     */
    @Override
    public int getProcessId() {
        return getProcessId0();
    }

    /**
     * Return the current thread id.
     */
    @Override
    public int getThreadId() {
        return getThreadId0();
    }
}
