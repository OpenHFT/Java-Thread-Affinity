package net.openhft.affinity.impl;

import com.sun.jna.Platform;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LinuxJNAAffinity implements IAffinity {
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxJNAAffinity.class);
    public static final boolean LOADED;

    // TODO: FIXME!!! CHANGE IAffinity TO SUPPORT PLATFORMS WITH 64+ CORES FIXME!!!
    @Override
    public long getAffinity() {
        final LinuxHelper.cpu_set_t cpuset = LinuxHelper.sched_getaffinity();
        return cpuset.__bits[0].longValue();
    }

    // TODO: FIXME!!! CHANGE IAffinity TO SUPPORT PLATFORMS WITH 64+ CORES FIXME!!!
    @Override
    public void setAffinity(final long affinity) {
        LinuxHelper.sched_setaffinity(affinity);
    }

    @Override
    public int getCpu() {
        return LinuxHelper.sched_getcpu();
    }

    private static final int PROCESS_ID;
    static {
        int pid = -1;
        try {
            pid = LinuxHelper.getpid();
        } catch (Exception ignored) {
        }
        PROCESS_ID = pid;
    }

    @Override
    public int getProcessId() {
        return PROCESS_ID;
    }

    private static final int SYS_gettid = Platform.is64Bit() ? 186 : 224;
    private static final Object[] NO_ARGS = {};
    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null)
            THREAD_ID.set(tid = LinuxHelper.syscall(SYS_gettid, NO_ARGS));
        return tid;
    }

    static {
        boolean loaded = false;
        try {
            INSTANCE.getAffinity();
            loaded = true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Unable to load jna library {}", e);
        }
        LOADED = loaded;
    }
}
