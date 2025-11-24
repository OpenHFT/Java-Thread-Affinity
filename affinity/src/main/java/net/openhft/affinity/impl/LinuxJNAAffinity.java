/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * Linux {@link IAffinity} implementation that delegates to libc via JNA.
 * <p>
 * Resolves process/thread ids, reads and sets CPU affinity masks, and caches thread ids per
 * thread. Guards against missing native libraries by exposing a {@link #LOADED} flag.
 */
public enum LinuxJNAAffinity implements IAffinity {
    INSTANCE;
    public static final boolean LOADED;
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxJNAAffinity.class);
    private static final int PROCESS_ID;
    private static final int SYS_gettid = Platform.isPPC() ? 207 : Platform.is64Bit() ? 186 : 224;
    private static final Object[] NO_ARGS = {};

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_LINUX = OS.startsWith("linux");

    static {
        int pid = -1;
        try {
            pid = LinuxHelper.getpid();
        } catch (NoClassDefFoundError | Exception ignored) {
            // best effort: leave pid as -1 if native helper is unavailable
        }
        PROCESS_ID = pid;
    }

    static {
        boolean loaded = false;
        try {
            INSTANCE.getAffinity();
            loaded = true;
        } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
            if (IS_LINUX)
                LOGGER.warn("Unable to load jna library", e);
        }
        LOADED = loaded;
    }

    private final ThreadLocal<Integer> threadId = new ThreadLocal<>();

    /**
     * Read the current affinity mask for this process.
     */
    @Override
    public BitSet getAffinity() {
        final LinuxHelper.cpu_set_t cpuset = LinuxHelper.sched_getaffinity();

        BitSet ret = new BitSet(LinuxHelper.cpu_set_t.__CPU_SETSIZE);
        int i = 0;
        for (NativeLong nl : cpuset.__bits) {
            for (int j = 0; j < Long.SIZE; j++)
                ret.set(i++, ((nl.longValue() >>> j) & 1) != 0);
        }
        return ret;
    }

    /**
     * Apply the given affinity mask to this process.
     */
    @Override
    public void setAffinity(final BitSet affinity) {
        LinuxHelper.sched_setaffinity(affinity);
    }

    /**
     * Return the current CPU id.
     */
    @Override
    public int getCpu() {
        return LinuxHelper.sched_getcpu();
    }

    /**
     * Cached process id obtained via {@link LinuxHelper#getpid()} where available.
     */
    @Override
    public int getProcessId() {
        return PROCESS_ID;
    }

    /**
     * Thread id resolved via {@code SYS_gettid}, cached per thread to avoid repeated syscalls.
     */
    @Override
    public int getThreadId() {
        Integer tid = threadId.get();
        if (tid == null)
            threadId.set(tid = LinuxHelper.syscall(SYS_gettid, NO_ARGS));
        return tid;
    }
}
