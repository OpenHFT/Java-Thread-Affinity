/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    private static final int SYS_gettid = Platform.is64Bit() ? 186 : 224;
    private static final Object[] NO_ARGS = {};

    static {
        int pid = -1;
        try {
            pid = LinuxHelper.getpid();
        } catch (Exception ignored) {
        }
        PROCESS_ID = pid;
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
