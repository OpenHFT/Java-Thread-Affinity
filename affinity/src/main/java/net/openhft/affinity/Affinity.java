/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * Library to wrap low level JNI or JNA calls.  Can be called without needing to know the actual implementation used.
 *
 * @author peter.lawrey
 */
public enum Affinity {
    ;
    static final Logger LOGGER = LoggerFactory.getLogger(Affinity.class);
    @NotNull
    private static final IAffinity AFFINITY_IMPL;
    private static Boolean JNAAvailable;

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
            LOGGER.warn("MAX OSX JNA-based affinity not usable due to JNA not being available!");
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

    public static AffinityLock acquireLock() {
        return isNonForkingAffinityAvailable() ? NonForkingAffinityLock.acquireLock() : AffinityLock.acquireLock();
    }

    public static AffinityLock acquireCore() {
        return isNonForkingAffinityAvailable() ? NonForkingAffinityLock.acquireCore() : AffinityLock.acquireCore();
    }

    public static AffinityLock acquireLock(boolean bind) {
        return isNonForkingAffinityAvailable() ? NonForkingAffinityLock.acquireLock(bind) : AffinityLock.acquireLock(bind);
    }

    public static AffinityLock acquireCore(boolean bind) {
        return isNonForkingAffinityAvailable() ? NonForkingAffinityLock.acquireCore(bind) : AffinityLock.acquireCore(bind);
    }

    private static boolean isNonForkingAffinityAvailable() {
        BootClassPath bootClassPath = BootClassPath.INSTANCE;
        return bootClassPath.has("java.lang.ThreadTrackingGroup") && bootClassPath.has("java.lang.ThreadLifecycleListener");
    }
}
