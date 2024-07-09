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

    // Logger instance for logging messages
    private static final Logger LOGGER = LoggerFactory.getLogger(LockCheck.class);

    // OS name in lowercase
    private static final String OS = System.getProperty("os.name").toLowerCase();

    // Boolean flag to check if the OS is Linux
    static final boolean IS_LINUX = OS.startsWith("linux");

    // Value representing an empty PID
    private static final int EMPTY_PID = Integer.MIN_VALUE;

    // LockChecker instance for managing locks
    private static final LockChecker lockChecker = FileLockBasedLockChecker.getInstance();

    /**
     * Retrieves the current process ID.
     *
     * @return the current process ID
     */
    public static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    /**
     * Checks if the current OS can support the operations.
     *
     * @return true if the OS is Linux, false otherwise
     */
    static boolean canOSSupportOperation() {
        return IS_LINUX;
    }

    /**
     * Checks if the specified CPU is free (not locked).
     *
     * @param cpu the CPU number to check
     * @return true if the CPU is free, false otherwise
     */
    public static boolean isCpuFree(int cpu) {
        if (!canOSSupportOperation())
            return true;

        return isLockFree(cpu);
    }

    /**
     * Replaces the PID for the specified CPU.
     *
     * @param cpu       the CPU number to replace the PID for
     * @param processID the new process ID
     * @return true if the PID was successfully replaced, false otherwise
     * @throws IOException if an I/O error occurs
     */
    static boolean replacePid(int cpu, long processID) throws IOException {
        return storePid(processID, cpu);
    }

    /**
     * Checks if a process with the specified PID is running.
     *
     * @param pid the process ID to check
     * @return true if the process is running, false otherwise
     */
    public static boolean isProcessRunning(long pid) {
        if (canOSSupportOperation())
            return new File("/proc/" + pid).exists();
        else
            throw new UnsupportedOperationException("this is only supported on LINUX");
    }

    /**
     * Stores the PID in a file named by the core. The PID is written to the file with the date below.
     *
     * @param processID the process ID to store
     * @param cpu       the CPU number to store the PID for
     * @return true if the PID was successfully stored, false otherwise
     * @throws IOException if an I/O error occurs
     */
    synchronized static boolean storePid(long processID, int cpu) throws IOException {
        return lockChecker.obtainLock(cpu, Long.toString(processID));
    }

    /**
     * Checks if the lock for the specified ID is free.
     *
     * @param id the ID to check
     * @return true if the lock is free, false otherwise
     */
    synchronized static boolean isLockFree(int id) {
        return lockChecker.isLockFree(id);
    }

    /**
     * Retrieves the process ID for the specified CPU core.
     *
     * @param core the CPU core number
     * @return the process ID for the specified core, or EMPTY_PID if not found
     * @throws IOException if an I/O error occurs
     */
    public static int getProcessForCpu(int core) throws IOException {
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

    static boolean updateCpu(int cpu) throws IOException {
        if (!canOSSupportOperation())
            return true;
        return replacePid(cpu, getPID());
    }

    public static void releaseLock(int cpu) {
        lockChecker.releaseLock(cpu);
    }
}
