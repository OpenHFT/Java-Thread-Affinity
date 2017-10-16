/*
 * Copyright 2016 higherfrequencytrading.com
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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Rob Austin.
 */
enum LockCheck {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(LockCheck.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final int EMPTY_PID = Integer.MIN_VALUE;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
    static final boolean IS_LINUX = OS.startsWith("linux");

    static long getPID() {
        String processName =
                java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    public static boolean isCpuFree(int cpu) {

        if (!IS_LINUX)
            return true;

        final File file = toFile(cpu);
        final boolean exists = file.exists();

        if (!exists) {
            return true;
        } else {
            int currentProcess = 0;
            try {
                currentProcess = getProcessForCpu(file);
            } catch (RuntimeException | IOException e) {
                LOGGER.warn("Failed to determine process on cpu " + cpu, e);
                e.printStackTrace();
                return true;
            }
            if (currentProcess == EMPTY_PID) {
                file.delete();
                return true;
            }
            if (!isProcessRunning(currentProcess)) {
                file.delete();
                return true;
            }
            return false;
        }
    }

    @NotNull
    static File toFile(int core) {
        return new File(tmpDir(), "cpu-" + core + ".lock");
    }

    static void replacePid(int core, long processID) throws IOException {
        replacePid(toFile(core), processID);
    }

    private static void replacePid(File file, long processID) throws IOException {
        file.delete();
        storePid(processID, file);
    }

    static boolean isProcessRunning(long pid) {
        if (IS_LINUX)
            return new File("/proc/" + pid).exists();
        else
            throw new UnsupportedOperationException("this is only supported on LINUX");
    }

    /**
     * stores the pid in a file, named by the core, the pid is written to the file with the date
     * below
     */
    private synchronized static void storePid(long processID, File coreFile) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(coreFile, false), "utf-8"))) {
            String processIDStr = Long.toString(processID);
            writer.write(processIDStr + "\n" + df.format(new Date()));
        }
    }

    static int getProcessForCpu(int core) throws IOException {
        return getProcessForCpu(toFile(core));
    }

    private static int getProcessForCpu(@NotNull File coreFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(coreFile), "utf-8"))) {

            final String firstLine = reader.readLine();
            if (firstLine == null) {
                LOGGER.warn("Empty lock file {}", coreFile.getAbsolutePath());
                return EMPTY_PID;
            }
            String s = firstLine.trim();
            try {
                return Integer.parseInt(s);
            } catch (RuntimeException e) {
                LOGGER.warn("Corrupt lock file {}: first line = '{}'", coreFile.getAbsolutePath(), firstLine);
                e.printStackTrace();
                return EMPTY_PID;
            }
        }
    }

    private static File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }

    static void updateCpu(int cpu) {
        if (!IS_LINUX)
            return;
        try {
            replacePid(toFile(cpu), getPID());
        } catch (IOException e) {
            LOGGER.warn("Failed to update lock file for cpu " + cpu, e);
            e.printStackTrace();
        }
    }
}