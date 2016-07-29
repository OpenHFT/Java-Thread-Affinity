package net.openhft.affinity;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Rob Austin.
 */
enum LockCheck {
    ;

    static final String TMP = System.getProperty("java.io.tmpdir");
    public static final String TARGET = System.getProperty("project.build.directory", findTarget());
    private static final String OS = System.getProperty("os.name").toLowerCase();
    static final boolean IS_LINUX = OS.startsWith("linux");
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");

    private static String findTarget() {
        for (File dir = new File(System.getProperty("user.dir")); dir != null; dir = dir.getParentFile()) {
            File target = new File(dir, "target");
            if (target.exists())
                return target.getAbsolutePath();
        }
        return TMP + "/target";
    }

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
            } catch (IOException e) {
                e.printStackTrace();
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
    private static File toFile(int core) {
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
                new FileOutputStream(coreFile), "utf-8"))) {
            String processIDStr = Long.toString(processID);
            writer.write(processIDStr + "\n" + df.format(new Date()));
        }
    }

    static int getProcessForCpu(int core) throws IOException {
        return getProcessForCpu(toFile(core));
    }

    private static int getProcessForCpu(@NotNull File coreFile) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(
                new BufferedReader(new InputStreamReader(new FileInputStream(coreFile), "utf-8")))) {
            String s = reader.readLine().trim();
            return Integer.parseInt(s);
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
            e.printStackTrace();
        }
    }


}
