package software.chronicle.enterprise.internals.impl;


import net.openhft.affinity.IAffinity;

public enum NativeAffinity implements IAffinity {
    INSTANCE;

    public static final boolean LOADED;

    static {
        LOADED = loadAffinityNativeLibrary();
    }

    private native static long getAffinity0();

    private native static void setAffinity0(long affinity);

    private native static int getCpu0();

    private native static int getProcessId0();

    private native static int getThreadId0();

    private static boolean loadAffinityNativeLibrary() {
        try {
            System.loadLibrary("CEInternals");
            return true;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public long getAffinity() {
        return getAffinity0();
    }

    @Override
    public void setAffinity(long affinity) {
        setAffinity0(affinity);
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
