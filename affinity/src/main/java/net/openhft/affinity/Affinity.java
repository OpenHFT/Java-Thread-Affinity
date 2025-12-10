/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

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
    ; // none
    static final Logger LOGGER = LoggerFactory.getLogger(Affinity.class);
    @NotNull
    private static final IAffinity AFFINITY_IMPL;
    private static volatile Boolean jnaAvailable;

    static {
        IAffinity impl;
        try {
            String osName = System.getProperty("os.name");
            if (osName.contains("Win") && isWindowsJNAAffinityUsable()) {
                LOGGER.trace("Using Windows JNA-based affinity control implementation");
                impl = WindowsJNAAffinity.INSTANCE;

            } else if (osName.contains("x")) {
                if (osName.startsWith("Linux") && isLinuxJNAAffinityUsable()) {
                    LOGGER.trace("Using Linux JNA-based affinity control implementation");
                    impl = LinuxJNAAffinity.INSTANCE;

                } else if (isPosixJNAAffinityUsable()) {
                    LOGGER.trace("Using Posix JNA-based affinity control implementation");
                    impl = PosixJNAAffinity.INSTANCE;

                } else {
                    LOGGER.info("Unsupported POSIX OS: {} with an 'x'. Using dummy affinity control implementation", osName);
                    impl = NullAffinity.INSTANCE;
                }
            } else if (osName.contains("Mac") && isMacJNAAffinityUsable()) {
                LOGGER.trace("Using MAC OSX JNA-based thread id implementation");
                impl = OSXJNAAffinity.INSTANCE;

            } else if (osName.contains("SunOS") && isSolarisJNAAffinityUsable()) {
                LOGGER.trace("Using Solaris JNA-based thread id implementation");
                impl = SolarisJNAAffinity.INSTANCE;

            } else {
                LOGGER.info("Unsupported OS: {}. Using dummy affinity control implementation", osName);
                impl = NullAffinity.INSTANCE;
            }
        } catch (Throwable t) {
            LOGGER.warn("Falling back to dummy affinity control implementation because native init failed", t);
            impl = NullAffinity.INSTANCE;
        }
        AFFINITY_IMPL = impl;
    }

    /**
     * Returns the platform-specific affinity implementation in use.
     *
     * @return current {@link IAffinity} implementation
     */
    public static IAffinity getAffinityImpl() {
        return AFFINITY_IMPL;
    }

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

    private static boolean isMacJNAAffinityUsable() {
        if (isJNAAvailable()) {
            return true;

        } else {
            LOGGER.warn("MAC OSX JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    private static boolean isSolarisJNAAffinityUsable() {
        if (isJNAAvailable()) {
            return true;

        } else {
            LOGGER.warn("Solaris JNA-based affinity not usable due to JNA not being available!");
            return false;
        }
    }

    private static void logThrowable(Throwable t, String description) {
        StringWriter sw = new StringWriter();
        sw.append(description);
        sw.append(" Reason: ");
        t.printStackTrace(new PrintWriter(sw));
        LOGGER.warn(sw.toString());
    }

    /**
     * Reads the current thread's CPU affinity mask.
     *
     * @return affinity bitset or {@code null} if unsupported
     */
    public static BitSet getAffinity() {
        IAffinity impl = AFFINITY_IMPL == null ? NullAffinity.INSTANCE : AFFINITY_IMPL;
        return impl.getAffinity();
    }

    /**
     * Applies the provided affinity mask to the current thread.
     *
     * @param affinity mask indicating allowed CPUs
     */
    public static void setAffinity(final BitSet affinity) {
        AFFINITY_IMPL.setAffinity(affinity);
    }

    /**
     * Binds the current thread to a single CPU.
     *
     * @param cpu logical CPU index
     */
    public static void setAffinity(int cpu) {
        BitSet affinity = new BitSet(Runtime.getRuntime().availableProcessors());
        affinity.set(cpu);
        setAffinity(affinity);
    }

    /**
     * Returns the logical CPU the current thread is running on, or -1 if unknown.
     *
     * @return cpu id or -1
     */
    public static int getCpu() {
        return AFFINITY_IMPL.getCpu();
    }

    /**
     * Returns the OS thread id of the current thread where available.
     *
     * @return native thread id or -1
     */
    public static int getThreadId() {
        return AFFINITY_IMPL.getThreadId();
    }

    /**
     * Propagates the native thread id into {@link Thread#tid} for logging/diagnostics.
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
     * Checks whether a compatible JNA version is on the classpath.
     *
     * @return {@code true} if JNA >= 5 is available
     */
    public static boolean isJNAAvailable() {
        Boolean available = jnaAvailable;
        if (available == null) {
            synchronized (Affinity.class) {
                available = jnaAvailable;
                if (available == null) {
                    boolean result;
                    try {
                        Class<?> nativeClass = Class.forName("com.sun.jna.Native");
                        Field versionField = nativeClass.getField("VERSION");
                        versionField.setAccessible(true);
                        Object versionObj = versionField.get(null);
                        String version = versionObj == null ? "0" : versionObj.toString();
                        int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                        if (majorVersion < 5) {
                            LOGGER.warn("Affinity library requires JNA version >= 5");
                            result = false;
                        } else {
                            try {
                                Class.forName("com.sun.jna.Platform");
                                result = true;
                            } catch (ClassNotFoundException ignored) {
                                result = false;
                            }
                        }
                    } catch (Throwable t) { // NoClassDefFoundError, UnsatisfiedLinkError, IllegalAccessException etc.
                        LOGGER.warn("JNA not available, falling back to NullAffinity", t);
                        result = false;
                    }
                    available = result;
                    jnaAvailable = available;
                }
            }
        }
        return available;
    }

    /**
     * Acquires an affinity lock, optionally binding the current thread.
     *
     * @return allocated {@link AffinityLock}
     */
    public static AffinityLock acquireLock() {
        return AffinityLock.acquireLock();
    }

    /**
     * Acquires a lock favouring whole cores rather than hyper-threads.
     *
     * @return allocated {@link AffinityLock}
     */
    public static AffinityLock acquireCore() {
        return AffinityLock.acquireCore();
    }

    /**
     * Acquires a lock and optionally binds the current thread immediately.
     *
     * @param bind whether to bind the thread to the lock's CPU
     * @return allocated {@link AffinityLock}
     */
    public static AffinityLock acquireLock(boolean bind) {
        return AffinityLock.acquireLock(bind);
    }

    /**
     * Acquires a core-oriented lock and optionally binds the current thread.
     *
     * @param bind whether to bind the thread to the lock's CPU
     * @return allocated {@link AffinityLock}
     */
    public static AffinityLock acquireCore(boolean bind) {
        return AffinityLock.acquireCore(bind);
    }

    /**
     * Restores the base affinity mask captured at startup.
     */
    public static void resetToBaseAffinity() {
        Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
    }
}
