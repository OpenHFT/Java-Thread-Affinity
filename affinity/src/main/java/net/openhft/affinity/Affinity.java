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

package net.openhft.affinity;

import com.sun.jna.Native;
import net.openhft.affinity.impl.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.BitSet;

/**
 * Library to wrap low level JNI or JNA calls.  Can be called without needing to know the actual
 * implementation used.
 *
 * @author peter.lawrey
 */
public enum Affinity {
    ; // No instances allowed

    // Logger instance for logging messages
    static final Logger LOGGER = LoggerFactory.getLogger(Affinity.class);

    // The implementation of the IAffinity interface
    @NotNull
    private static final IAffinity AFFINITY_IMPL;

    // Boolean to check if JNA is available
    private static Boolean JNAAvailable;

    // Static block to initialize the AFFINITY_IMPL based on the OS
    static {
        String osName = System.getProperty("os.name");
        if (osName.contains("Win") && isWindowsJNAAffinityUsable()) {
            LOGGER.trace("Using Windows JNA-based affinity control implementation");
            AFFINITY_IMPL = WindowsJNAAffinity.INSTANCE;

        } else if (osName.contains("x")) {
            /*if (osName.startsWith("Linux") && NativeAffinity.LOADED) {
                LOGGER.trace("Using Linux JNI-based affinity control implementation");
                AFFINITY_IMPL = NativeAffinity.INSTANCE;
            } else*/
            if (osName.startsWith("Linux") && isLinuxJNAAffinityUsable()) {
                LOGGER.trace("Using Linux JNA-based affinity control implementation");
                AFFINITY_IMPL = LinuxJNAAffinity.INSTANCE;

            } else if (isPosixJNAAffinityUsable()) {
                LOGGER.trace("Using Posix JNA-based affinity control implementation");
                AFFINITY_IMPL = PosixJNAAffinity.INSTANCE;

            } else {
                LOGGER.info("Using dummy affinity control implementation");
                AFFINITY_IMPL = NullAffinity.INSTANCE;
            }
        } else if (osName.contains("Mac") && isMacJNAAffinityUsable()) {
            LOGGER.trace("Using MAC OSX JNA-based thread id implementation");
            AFFINITY_IMPL = OSXJNAAffinity.INSTANCE;

        } else if (osName.contains("SunOS") && isSolarisJNAAffinityUsable()) {
            LOGGER.trace("Using Solaris JNA-based thread id implementation");
            AFFINITY_IMPL = SolarisJNAAffinity.INSTANCE;

        } else {
            LOGGER.info("Using dummy affinity control implementation");
            AFFINITY_IMPL = NullAffinity.INSTANCE;
        }
    }

    /**
     * Returns the current implementation of the IAffinity interface.
     *
     * @return the current implementation of the IAffinity interface
     */
    public static IAffinity getAffinityImpl() {
        return AFFINITY_IMPL;
    }

    /**
     * Checks if the Windows JNA-based affinity is usable.
     *
     * @return true if usable, false otherwise
     */
    private static boolean isWindowsJNAAffinityUsable() {
        if (isJNAAvailable()) {
            try {
                return WindowsJNAAffinity.LOADED;
            } catch (Throwable t) {
                logThrowable(t, "Windows JNA-based affinity not usable because it failed to load!");
                return false;
            }
        } else {
            LOGGER.warn("Windows JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    /**
     * Checks if the Posix JNA-based affinity is usable.
     *
     * @return true if usable, false otherwise
     */
    private static boolean isPosixJNAAffinityUsable() {
        if (isJNAAvailable()) {
            try {
                return PosixJNAAffinity.LOADED;
            } catch (Throwable t) {
                logThrowable(t, "Posix JNA-based affinity not usable because it failed to load!");
                return false;
            }
        } else {
            LOGGER.warn("Posix JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    /**
     * Checks if the Linux JNA-based affinity is usable.
     *
     * @return true if usable, false otherwise
     */
    private static boolean isLinuxJNAAffinityUsable() {
        if (isJNAAvailable()) {
            try {
                return LinuxJNAAffinity.LOADED;
            } catch (Throwable t) {
                logThrowable(t, "Linux JNA-based affinity not usable because it failed to load!");
                return false;
            }
        } else {
            LOGGER.warn("Linux JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    /**
     * Checks if the Mac JNA-based affinity is usable.
     *
     * @return true if usable, false otherwise
     */
    private static boolean isMacJNAAffinityUsable() {
        if (isJNAAvailable()) {
            return true;
        } else {
            LOGGER.warn("MAC OSX JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    /**
     * Checks if the Solaris JNA-based affinity is usable.
     *
     * @return true if usable, false otherwise
     */
    private static boolean isSolarisJNAAffinityUsable() {
        if (isJNAAvailable()) {
            return true;
        } else {
            LOGGER.warn("Solaris JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    /**
     * Logs the throwable with a given description.
     *
     * @param t           the throwable to log
     * @param description the description to log with the throwable
     */
    private static void logThrowable(Throwable t, String description) {
        StringWriter sw = new StringWriter();
        sw.append(description);
        sw.append(" Reason: ");
        t.printStackTrace(new PrintWriter(sw));
        LOGGER.warn(sw.toString());
    }

    /**
     * Gets the current CPU affinity as a BitSet.
     *
     * @return the current CPU affinity as a BitSet
     */
    public static BitSet getAffinity() {
        return AFFINITY_IMPL.getAffinity();
    }

    /**
     * Sets the CPU affinity to the given BitSet.
     *
     * @param affinity the BitSet representing the CPU affinity to set
     */
    public static void setAffinity(final BitSet affinity) {
        AFFINITY_IMPL.setAffinity(affinity);
    }

    /**
     * Sets the CPU affinity to the given CPU.
     *
     * @param cpu the CPU to set the affinity for
     */
    public static void setAffinity(int cpu) {
        BitSet affinity = new BitSet(Runtime.getRuntime().availableProcessors());
        affinity.set(cpu);
        setAffinity(affinity);
    }

    /**
     * Gets the current CPU ID.
     *
     * @return the current CPU ID
     */
    public static int getCpu() {
        return AFFINITY_IMPL.getCpu();
    }

    /**
     * Gets the current thread ID.
     *
     * @return the current thread ID
     */
    public static int getThreadId() {
        return AFFINITY_IMPL.getThreadId();
    }

    /**
     * Sets the thread ID for the current thread.
     */
    public static void setThreadId() {
        try {
            int threadId = Affinity.getThreadId();
            final Field tid = Thread.class.getDeclaredField("tid");
            tid.setAccessible(true);
            final Thread thread = Thread.currentThread();
            tid.setLong(thread, threadId);
            Affinity.LOGGER.info("Set {} to thread id {}", thread.getName(), threadId);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Checks if JNA is available.
     *
     * @return true if JNA is available, false otherwise
     */
    public static boolean isJNAAvailable() {
        if (JNAAvailable == null) {
            int majorVersion = Integer.parseInt(Native.VERSION.split("\\.")[0]);
            if (majorVersion < 5) {
                LOGGER.warn("Affinity library requires JNA version >= 5");
                JNAAvailable = false;
            } else {
                try {
                    Class.forName("com.sun.jna.Platform");
                    JNAAvailable = true;
                } catch (ClassNotFoundException ignored) {
                    JNAAvailable = false;
                }
            }
        }
        return JNAAvailable;
    }

    /**
     * Acquires an affinity lock.
     *
     * @return the acquired AffinityLock
     */
    public static AffinityLock acquireLock() {
        return AffinityLock.acquireLock();
    }

    /**
     * Acquires a core affinity lock.
     *
     * @return the acquired core AffinityLock
     */
    public static AffinityLock acquireCore() {
        return AffinityLock.acquireCore();
    }

    /**
     * Acquires an affinity lock, optionally binding it.
     *
     * @param bind whether to bind the lock
     * @return the acquired AffinityLock
     */
    public static AffinityLock acquireLock(boolean bind) {
        return AffinityLock.acquireLock(bind);
    }

    /**
     * Acquires a core affinity lock, optionally binding it.
     *
     * @param bind whether to bind the lock
     * @return the acquired core AffinityLock
     */
    public static AffinityLock acquireCore(boolean bind) {
        return AffinityLock.acquireCore(bind);
    }

    /**
     * Resets to the base affinity.
     */
    public static void resetToBaseAffinity() {
        Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
    }
}
