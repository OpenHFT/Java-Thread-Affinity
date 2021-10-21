package net.openhft.affinity.lockchecker;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileBasedLockChecker implements LockChecker {

    static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedLockChecker.class);
    private static final LockChecker instance = new FileBasedLockChecker();

    protected FileBasedLockChecker() {
        //nothing
    }

    public static LockChecker getInstance() {
        return instance;
    }

    private static File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }

    @Override
    public boolean isLockFree(int id) {
        return !toFile(id).exists();
    }

    @Override
    public boolean obtainLock(int id, String metaInfo) throws IOException {
        File file = toFile(id);
        file.delete();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file, false), UTF_8))) {
            writer.write(metaInfo + "\n" + df.format(new Date()));
            file.setWritable(true, false);
            file.setExecutable(false, false);
            return true;
        }
    }

    @Override
    public boolean releaseLock(int id) {
        return toFile(id).delete();
    }

    @Override
    public String getMetaInfo(int id) throws IOException {
        File file = toFile(id);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
            final String firstLine = reader.readLine();
            if (firstLine == null) {
                LOGGER.error(String.format("Empty lock file %s%n", file.getAbsolutePath()));
                return null;
            }
            return firstLine.trim();
        }
    }

    @NotNull
    protected File toFile(int id) {
        assert id >= 0;
        return new File(tmpDir(), "cpu-" + id + ".lock");
    }
}
