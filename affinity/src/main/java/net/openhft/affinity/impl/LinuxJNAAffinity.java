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

    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

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

    // TODO: FIXME!!! CHANGE IAffinity TO SUPPORT PLATFORMS WITH 64+ CORES FIXME!!!
    @Override
    public void setAffinity(final BitSet affinity) {
        LinuxHelper.sched_setaffinity(affinity);
    }

    @Override
    public int getCpu() {
        return LinuxHelper.sched_getcpu();
    }

    @Override
    public int getProcessId() {
        return PROCESS_ID;
    }

    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null)
            THREAD_ID.set(tid = LinuxHelper.syscall(SYS_gettid, NO_ARGS));
        return tid;
    }
}
