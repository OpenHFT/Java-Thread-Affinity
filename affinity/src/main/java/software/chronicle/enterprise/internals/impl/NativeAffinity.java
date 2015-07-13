package software.chronicle.enterprise.internals.impl;


import net.openhft.affinity.IAffinity;

import java.util.BitSet;

public enum NativeAffinity implements IAffinity {
    INSTANCE;

    public static final boolean LOADED;

    static {
        LOADED = loadAffinityNativeLibrary();
    }

    private native static byte[] getAffinity0();

    private native static void setAffinity0(byte[] affinity);

    private native static int getCpu0();

    private native static int getProcessId0();

    private native static int getThreadId0();

    private native static long rdtsc0();

    private static boolean loadAffinityNativeLibrary() {
        try {
            System.loadLibrary("CEInternals");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public BitSet getAffinity()
    {
        final byte[] buff = getAffinity0();
        if (buff == null)
        {
            return null;
        }
        return BitSet.valueOf(buff);
    }

    @Override
    public void setAffinity(BitSet affinity)
    {
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
