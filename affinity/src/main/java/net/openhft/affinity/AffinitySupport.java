/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import net.openhft.affinity.impl.OSXJNAAffinity;
import net.openhft.affinity.impl.PosixJNAAffinity;
import net.openhft.affinity.impl.WindowsJNAAffinity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * Library to wrap low level JNI or JNA calls.  Can be called without needing to know the actual implementation used.
 *
 * @author peter.lawrey
 */
public enum AffinitySupport {
    ;
    @NotNull
    private static final IAffinity AFFINITY_IMPL;
    private static final Logger LOGGER = Logger.getLogger(AffinitySupport.class.getName());
    private static Boolean JNAAvailable;

    static {
        String osName = System.getProperty("os.name");
        if (osName.contains("Win") && isJNAAvailable() && WindowsJNAAffinity.LOADED) {
            LOGGER.fine("Using Windows JNA-based affinity control implementation");
            AFFINITY_IMPL = WindowsJNAAffinity.INSTANCE;
        } else if (osName.contains("x") && isJNAAvailable() && PosixJNAAffinity.LOADED) {
            LOGGER.fine("Using Posix JNA-based affinity control implementation");
            AFFINITY_IMPL = PosixJNAAffinity.INSTANCE;
        }else if (osName.contains("Mac") && isJNAAvailable()) {
            LOGGER.fine("Using MAC OSX JNA-based thread id implementation");
            AFFINITY_IMPL = OSXJNAAffinity.INSTANCE;
        }
        else {
            LOGGER.info("Using dummy affinity control implementation");
            AFFINITY_IMPL = NullAffinity.INSTANCE;
        }
    }

    public static long getAffinity() {
        return AFFINITY_IMPL.getAffinity();
    }

    public static void setAffinity(final long affinity) {
        AFFINITY_IMPL.setAffinity(affinity);
    }

    public static int getCpu() {
        return AFFINITY_IMPL.getCpu();
    }

    public static int getThreadId() {
        return AFFINITY_IMPL.getThreadId();
    }

    public static boolean isJNAAvailable() {
        if (JNAAvailable == null)
            try {
                Class.forName("com.sun.jna.Platform");
                JNAAvailable = true;
            } catch (ClassNotFoundException ignored) {
                JNAAvailable = false;
            }
        return JNAAvailable;
    }

    public static void setThreadId() {
        try {
            int threadId = getThreadId();
            final Field tid = Thread.class.getDeclaredField("tid");
            tid.setAccessible(true);
            final Thread thread = Thread.currentThread();
            tid.setLong(thread, threadId);
            Logger.getAnonymousLogger().info("Set " + thread.getName() + " to thread id " + threadId);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
