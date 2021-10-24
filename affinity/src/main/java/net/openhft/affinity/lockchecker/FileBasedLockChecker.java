package net.openhft.affinity.lockchecker;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.*;
import static net.openhft.affinity.LockCheck.getProcessForCpu;
import static net.openhft.affinity.LockCheck.isProcessRunning;
import static net.openhft.affinity.impl.VanillaCpuLayout.MAX_CPUS_SUPPORTED;

public class FileBasedLockChecker implements LockChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedLockChecker.class);
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");

    private static final LockChecker instance = new FileBasedLockChecker();
    private static final Set<StandardOpenOption> openOptions = new HashSet<>(Arrays.asList(CREATE_NEW, WRITE, READ, SYNC));
    private static final FileAttribute<Set<PosixFilePermission>> fileAttr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw-rw-"));
    private final LockReference[] locks = new LockReference[MAX_CPUS_SUPPORTED];

    protected FileBasedLockChecker() {
        //nothing
    }

    public static LockChecker getInstance() {
        return instance;
    }

    @Override
    public boolean isLockFree(int id) {
        return isLockFree(toFile(id), id);
    }

    private boolean isLockFree(File file, int id) {
        //do we have the lock already?
        LockReference existingLock = locks[id];
        if (existingLock != null) {
            return false;
        }

        // if the file doesn't exist, no lock is held
        if (!file.exists()) {
            return true;
        }

        // if the process that created the file is not running, no lock is held
        int currentProcess = 0;
        try {
            currentProcess = getProcessForCpu(id);
        } catch (RuntimeException | IOException e) {
            LOGGER.warn("Failed to determine process on cpu " + id, e);
            e.printStackTrace();
            return true;
        }
        return !isProcessRunning(currentProcess);
    }

    @Override
    public boolean obtainLock(int id, String metaInfo) throws IOException {
        final File file = toFile(id);
        if (!isLockFree(file, id)) {
            return false;
        } else {
            file.delete();
        }

        FileChannel fc = FileChannel.open(file.toPath(), openOptions, fileAttr);

        LOGGER.debug(String.format("Successfully created lock file %s (%s)%n", file.getAbsolutePath(), metaInfo));
        locks[id] = new LockReference(fc, null);
        byte[] content = String.format("%s%n%s", metaInfo, df.format(new Date())).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(content);
        while (buffer.hasRemaining()) {
            fc.write(buffer);
        }
        return true;
    }

    @Override
    public boolean releaseLock(int id) {
        try {
            LockReference lock = locks[id];
            if (lock != null) {
                locks[id] = null;
                lock.channel.close();
                toFile(id).delete();
                return true;
            }
            return false;
        } catch (IOException e) {
            LOGGER.error(String.format("Couldn't release lock for id %d due to exception: %s%n", id, e.getMessage()));
            return false;
        }
    }

    @Override
    public String getMetaInfo(int id) throws IOException {
        final File file = toFile(id);

        LockReference lr = locks[id];
        if (lr != null) {
            return readMetaInfoFromLockFileChannel(file, lr.channel);
        } else {
            try (FileChannel fc = FileChannel.open(file.toPath(), READ)) {
                return readMetaInfoFromLockFileChannel(file, fc);
            } catch (NoSuchFileException e) {
                return null;
            }
        }
    }

    private String readMetaInfoFromLockFileChannel(File lockFile, FileChannel lockFileChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        int len = lockFileChannel.read(buffer, 0);
        String content = len < 1 ? "" : new String(buffer.array(), 0, len);
        if (content.isEmpty()) {
            LOGGER.warn("Empty lock file {}", lockFile.getAbsolutePath());
            return null;
        }
        return content.substring(0, content.indexOf("\n"));
    }

    @NotNull
    protected File toFile(int id) {
        assert id >= 0;
        File file = new File(tmpDir(), "cpu-" + id + ".lock");
        return file;
    }

    private File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }
}
