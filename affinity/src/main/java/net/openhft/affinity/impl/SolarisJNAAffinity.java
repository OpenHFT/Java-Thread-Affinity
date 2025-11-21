/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import net.openhft.affinity.IAffinity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ThreadLocal<Integer> threadId = new ThreadLocal<>();

    @Override
    public BitSet getAffinity() {
        return new BitSet();
    }

    @Override
    public void setAffinity(final BitSet affinity) {
        LOGGER.trace("unable to set mask to {} as the JNI and JNA libraries not loaded", Utilities.toHexString(affinity));
    }

    @Override
    public int getCpu() {
        return -1;
    }

    @Override
    public int getProcessId() {
        return Utilities.currentProcessId();
    }

    @Override
    public int getThreadId() {
        Integer tid = threadId.get();
        if (tid == null) {
            tid = CLibrary.INSTANCE.pthread_self();
            //The tid assumed to be an unsigned 24 bit, see net.openhft.lang.Jvm.getMaxPid()
            tid = tid & 0xFFFFFF;
            threadId.set(tid);
        }
        return tid;
    }

    interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int pthread_self() throws LastErrorException;
    }
}
