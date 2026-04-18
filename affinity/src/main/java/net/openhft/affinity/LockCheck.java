/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.Utilities;
import net.openhft.affinity.lockchecker.FileLockBasedLockChecker;
import net.openhft.affinity.lockchecker.LockChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Rob Austin.
 */
public enum LockCheck {
    ; // none

    private static final Logger LOGGER = LoggerFactory.getLogger(LockCheck.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    static final boolean IS_LINUX = OS.startsWith("linux");
    private static final int EMPTY_PID = Integer.MIN_VALUE;

    private static final LockChecker lockChecker = FileLockBasedLockChecker.getInstance();

    public static long getPID() {
        return Utilities.currentProcessId();
    }

    static boolean canOSSupportOperation() {
        return IS_LINUX;
    }

    public static boolean isCpuFree(int cpu) {
        if (!canOSSupportOperation())
            return true;

        return isLockFree(cpu);
    }

    static boolean replacePid(int cpu, int cpu2, long processID) throws IOException {
        return storePid(processID, cpu, cpu2);
    }

    public static boolean isProcessRunning(long pid) {
        if (canOSSupportOperation())
            return new File("/proc/" + pid).exists();
        else
            throw new UnsupportedOperationException("this is only supported on LINUX");
    }

    /**
     * stores the pid in a file, named by the core, the pid is written to the file with the date
     * below
     */
    private synchronized static boolean storePid(long processID, int cpu, int cpu2) throws IOException {
        return lockChecker.obtainLock(cpu, cpu2, Long.toString(processID));
    }

    private synchronized static boolean isLockFree(int id) {
        return lockChecker.isLockFree(id);
    }

    public static int getProcessForCpu(int core) throws IOException {
        if (!canOSSupportOperation())
            return EMPTY_PID;

        String meta = lockChecker.getMetaInfo(core);

        if (meta != null && !meta.isEmpty()) {
            try {
                return Integer.parseInt(meta);
            } catch (NumberFormatException e) {
                //nothing
            }
        }
        return EMPTY_PID;
    }

    static boolean updateCpu(int cpu, int cpu2) throws IOException {
        if (!canOSSupportOperation())
            return true;
        return replacePid(cpu, cpu2, getPID());
    }

    public static void releaseLock(int cpu) {
        lockChecker.releaseLock(cpu);
    }
}
