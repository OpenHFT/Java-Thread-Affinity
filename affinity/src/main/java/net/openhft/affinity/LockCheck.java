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

    private static final Logger LOGGER = LoggerFactory.getLogger(LockCheck.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    static final boolean IS_LINUX = OS.startsWith("linux");
    private static final int EMPTY_PID = Integer.MIN_VALUE;

    private static final LockChecker lockChecker = FileLockBasedLockChecker.getInstance();

    public static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    static boolean canOSSupportOperation() {
        return IS_LINUX;
    }

    public static boolean isCpuFree(int cpu) {
        if (!canOSSupportOperation())
            return true;

        return isLockFree(cpu);
    }

    static boolean replacePid(int cpu, long processID) throws IOException {
        return storePid(processID, cpu);
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
    private synchronized static boolean storePid(long processID, int cpu) throws IOException {
        return lockChecker.obtainLock(cpu, Long.toString(processID));
    }

    private synchronized static boolean isLockFree(int id) {
        return lockChecker.isLockFree(id);
    }

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
