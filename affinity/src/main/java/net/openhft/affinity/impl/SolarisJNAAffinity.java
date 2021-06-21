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

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
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
public enum SolarisJNAAffinity implements IAffinity {
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolarisJNAAffinity.class);
    private final ThreadLocal<Integer> THREAD_ID = new ThreadLocal<>();

    @Override
    public BitSet getAffinity() {
        return new BitSet();
    }

    @Override
    public void setAffinity(final BitSet affinity) {
        LOGGER.trace("unable to set mask to {} as the JNIa nd JNA libraries and not loaded", Utilities.toHexString(affinity));
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
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int pthread_self() throws LastErrorException;
    }
}
