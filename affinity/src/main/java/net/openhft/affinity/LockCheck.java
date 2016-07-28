package net.openhft.affinity;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Rob Austin.
 */
class LockCheck {

    static final String TMP = System.getProperty("java.io.tmpdir");
    private static final String OS = System.getProperty("os.name").toLowerCase();
    static final boolean IS_LINUX = OS.startsWith("linux");

    private ThreadLocal<SimpleDateFormat> df = new ThreadLocal() {

        @Override
        protected Object initialValue() {
            return new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
        }
    };

    private static String findTarget() {
        for (File dir = new File(System.getProperty("user.dir")); dir != null; dir = dir.getParentFile()) {
            File target = new File(dir, "target");
            if (target.exists())
                return target.getAbsolutePath();
        }
        return TMP + "/target";
    }

    /**
     * stores the process id for a give pid
     *
     * @param core
     * @param processID
     * @return
     * @throws IOException
     */
    boolean isCoreAlreadyAssigned(int core, long processID) throws IOException {

        final File file = new File(tmpDir(), "core-" + core + ".lock");
        final boolean exists = file.exists();

        if (!exists) {
            // create a lock file
            storePid(processID, file);
            return true;
        } else {
            int currentProcess = getPid(file);
            if (!isProcessRunning(currentProcess)) {
                replacePid(processID, file);
                return true;
            }
            return false;
        }
    }

    private void replacePid(long processID, File file) throws IOException {
        file.delete();
        storePid(processID, file);
    }

    boolean isProcessRunning(long pid) {
        if (IS_LINUX)
            return new File("/proc/" + pid).exists();
        else
            throw new UnsupportedOperationException("this is only supported on LINUX");
    }

    /**
     * stores the pid in a file, named by the core, the pid is written to the file with the date
     * below
     *
     * @param processID
     * @param coreFile
     * @throws IOException
     */
    private void storePid(long processID, File coreFile) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(coreFile), "utf-8"))) {
            String processIDStr = Long.toString(processID);
            writer.write(processIDStr + "\n" + df.get().format(new Date()));
        }
    }

    int getPid(int core) throws IOException {
        return getPid(new File(tmpDir(), "core-" + core + ".lock"));
    }

    private int getPid(@NotNull File coreFile) throws IOException {

        try (LineNumberReader reader = new LineNumberReader(
                new BufferedReader(new InputStreamReader(new FileInputStream(coreFile), "utf-8")))) {
            String s = reader.readLine().trim();
            System.out.println("" + s);
            return Integer.parseInt(s);
        }
    }

    private File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }

}
