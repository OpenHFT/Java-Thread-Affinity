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
import java.lang.invoke.MethodHandles;
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
        } catch (LinkageError | RuntimeException t) {
            // Optional native initialisation may fail; VM errors and assertion failures must propagate.
            LOGGER.warn("Falling back to dummy affinity control implementation because native init failed", t);
            impl = NullAffinity.INSTANCE;
        }
        AFFINITY_IMPL = impl;
    }

    public static IAffinity getAffinityImpl() {
        return AFFINITY_IMPL;
    }

    private static boolean isWindowsJNAAffinityUsable() {
        if (isJNAAvailable()) {
            try {
                return WindowsJNAAffinity.LOADED;
            } catch (LinkageError | RuntimeException t) {
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
            } catch (LinkageError | RuntimeException t) {
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
            } catch (LinkageError | RuntimeException t) {
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

    public static BitSet getAffinity() {
        return AFFINITY_IMPL.getAffinity();
    }

    public static void setAffinity(final BitSet affinity) {
        AFFINITY_IMPL.setAffinity(affinity);
    }

    public static void setAffinity(int cpu) {
        BitSet affinity = new BitSet(Runtime.getRuntime().availableProcessors());
        affinity.set(cpu);
        setAffinity(affinity);
    }

    public static int getCpu() {
        return AFFINITY_IMPL.getCpu();
    }

    public static int getThreadId() {
        return AFFINITY_IMPL.getThreadId();
    }

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

    @SuppressWarnings("removal") // ThreadDeath must propagate on supported older JDKs.
    public static boolean isJNAAvailable() {
        Boolean available = jnaAvailable;
        if (available == null) {
            synchronized (Affinity.class) {
                available = jnaAvailable;
                if (available == null) {
                    boolean result;
                    try {
                        Class<?> nativeClass = Class.forName("com.sun.jna.Native");
                        // Access the inherited public field through Native without opening JNA's module.
                        String version = (String) MethodHandles.publicLookup()
                                .findStaticGetter(nativeClass, "VERSION", String.class).invokeExact();
                        int majorVersion = version == null ? 0 : Integer.parseInt(version.split("\\.")[0]);
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
                    } catch (VirtualMachineError | ThreadDeath | AssertionError fatal) {
                        throw fatal;
                    } catch (Throwable t) {
                        // JNA also reports an incompatible jnidispatch with a plain Error.
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

    public static AffinityLock acquireLock() {
        return AffinityLock.acquireLock();
    }

    public static AffinityLock acquireCore() {
        return AffinityLock.acquireCore();
    }

    public static AffinityLock acquireLock(boolean bind) {
        return AffinityLock.acquireLock(bind);
    }

    public static AffinityLock acquireCore(boolean bind) {
        return AffinityLock.acquireCore(bind);
    }

    public static void resetToBaseAffinity() {
        Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
    }
}
