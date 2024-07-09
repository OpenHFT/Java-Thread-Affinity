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

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * The LinuxJNAAffinity class provides an implementation of the IAffinity interface for Linux systems using JNA.
 * It allows setting and getting CPU affinity and retrieving process and thread IDs.
 * <p>
 * Author: Peter Lawrey
 * </p>
 */
public enum LinuxJNAAffinity implements IAffinity {
    INSTANCE;

    // Indicates whether the JNA library is loaded
    public static final boolean LOADED;

    // Logger instance for logging messages
    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxJNAAffinity.class);

    // Process ID of the current process
    private static final int PROCESS_ID;

    // System call number for getting thread ID
    private static final int SYS_gettid = Platform.isPPC() ? 207 : Platform.is64Bit() ? 186 : 224;

    // No arguments for syscall
    private static final Object[] NO_ARGS = {};

    // Operating system name
    private static final String OS = System.getProperty("os.name").toLowerCase();

    // Flag to check if the operating system is Linux
    private static final boolean IS_LINUX = OS.startsWith("linux");

    static {
        int pid = -1;
        try {
            pid = LinuxHelper.getpid();
        } catch (NoClassDefFoundError | Exception ignored) {
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
                LOGGER.warn("Unable to load jna library {}", e);
        }
        LOADED = loaded;
    }

    // ThreadLocal variable to store thread IDs
    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    /**
     * Gets the CPU affinity of the current process.
     *
     * @return a BitSet representing the CPU affinity
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
     * Sets the CPU affinity for the current process.
     *
     * @param affinity the BitSet representing the CPU affinity
     */
    @Override
    public void setAffinity(final BitSet affinity) {
        LinuxHelper.sched_setaffinity(affinity);
    }

    /**
     * Gets the current CPU that the calling thread is running on.
     *
     * @return the CPU number
     */
    @Override
    public int getCpu() {
        return LinuxHelper.sched_getcpu();
    }

    /**
     * Gets the process ID of the current process.
     *
     * @return the process ID
     */
    @Override
    public int getProcessId() {
        return PROCESS_ID;
    }

    /**
     * Gets the thread ID of the calling thread.
     *
     * @return the thread ID
     */
    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null)
            THREAD_ID.set(tid = LinuxHelper.syscall(SYS_gettid, NO_ARGS));
        return tid;
    }
}
