/*
 * Copyright 2016-2025 chronicle.software
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

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.thread_affinity_policy_data_t;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.BitSet;

/**
 * This is essentially the same as the NullAffinity implementation but with concrete
 * support for getThreadId().
 *
 * @author daniel.shaya
 */
public enum OSXJNAAffinity implements IAffinity {
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(OSXJNAAffinity.class);
    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    private static final int THREAD_AFFINITY_POLICY = 4; // from thread_policy.h
    private static final int THREAD_AFFINITY_POLICY_COUNT = 1;

    @Override
    public BitSet getAffinity() {
        if (!Platform.isMac())
            return new BitSet();
        IntByReference count = new IntByReference(THREAD_AFFINITY_POLICY_COUNT);
        IntByReference def = new IntByReference();
        thread_affinity_policy_data_t policy = new thread_affinity_policy_data_t();
        int thread = SystemB.INSTANCE.pthread_mach_thread_np(CLibrary.INSTANCE.pthread_self());
        int rc = SystemB.INSTANCE.thread_policy_get(thread, THREAD_AFFINITY_POLICY, policy, count, def);
        SystemB.INSTANCE.mach_port_deallocate(SystemB.INSTANCE.mach_task_self(), thread);
        if (rc != 0) {
            LOGGER.warn("thread_policy_get rc=" + rc);
            return new BitSet();
        }
        BitSet bs = new BitSet();
        if (policy.affinity_tag >= 0)
            bs.set(policy.affinity_tag);
        return bs;
    }

    @Override
    public void setAffinity(final BitSet affinity) {
        if (!Platform.isMac()) {
            LOGGER.trace("Non Mac platform - ignoring setAffinity");
            return;
        }
        long[] arr = affinity.toLongArray();
        int tag = arr.length > 0 ? (int) arr[0] : 0;
        thread_affinity_policy_data_t policy = new thread_affinity_policy_data_t();
        policy.affinity_tag = tag;
        int thread = SystemB.INSTANCE.pthread_mach_thread_np(CLibrary.INSTANCE.pthread_self());
        int rc = SystemB.INSTANCE.thread_policy_set(thread, THREAD_AFFINITY_POLICY, policy, THREAD_AFFINITY_POLICY_COUNT);
        SystemB.INSTANCE.mach_port_deallocate(SystemB.INSTANCE.mach_task_self(), thread);
        if (rc != 0)
            LOGGER.warn("thread_policy_set rc=" + rc);
    }

    @Override
    public int getCpu() {
        return -1;
    }

    @Override
    public int getProcessId() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    @Override
    public int getThreadId() {
        Integer tid = THREAD_ID.get();
        if (tid == null) {
            tid = CLibrary.INSTANCE.pthread_self();
            //The tid assumed to be an unsigned 24 bit, see net.openhft.lang.Jvm.getMaxPid()
            tid = tid & 0xFFFFFF;
            THREAD_ID.set(tid);
        }
        return tid;
    }

    interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("System", CLibrary.class);

        int pthread_self() throws LastErrorException;

        int pthread_mach_thread_np(int pthread);
    }
}
