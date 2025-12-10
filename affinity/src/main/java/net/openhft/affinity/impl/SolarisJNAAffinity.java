/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntSupplier;

/**
 * This is essentially the same as the NullAffinity implementation but with concrete
 * support for getThreadId().
 *
 * @author daniel.shaya
 */
public enum SolarisJNAAffinity implements NoAffinity {
    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SolarisJNAAffinity.class);
    private final ThreadLocal<Integer> threadId = new ThreadLocal<>();

    @Override
    public Logger logger() {
        return LOGGER;
    }

    @Override
    public ThreadLocal<Integer> threadIdCache() {
        return threadId;
    }

    @Override
    public IntSupplier threadIdSupplier() {
        return CLibrary.INSTANCE::pthread_self;
    }

    interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        int pthread_self() throws LastErrorException;
    }
}
