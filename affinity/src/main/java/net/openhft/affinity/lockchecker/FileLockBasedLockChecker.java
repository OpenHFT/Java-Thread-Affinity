package net.openhft.affinity.lockchecker;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
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
import static net.openhft.affinity.impl.VanillaCpuLayout.MAX_CPUS_SUPPORTED;

public class FileLockBasedLockChecker implements LockChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLockBasedLockChecker.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");

    private static final LockChecker instance = new FileLockBasedLockChecker();
    private static final HashSet<StandardOpenOption> openOptions = new HashSet<>(Arrays.asList(CREATE, WRITE, READ, SYNC));
    private static final FileAttribute<Set<PosixFilePermission>> fileAttr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw-rw-"));
    private final LockReference[] locks = new LockReference[MAX_CPUS_SUPPORTED];

    protected FileLockBasedLockChecker() {
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

        //does another process have the lock?
        try {
            // only take a shared lock to test if the file is locked,
            // this means processes testing the lock concurrently
            // won't interfere with each other
            try (FileChannel fc = FileChannel.open(file.toPath(), READ);
                 FileLock fileLock = fc.tryLock(0, Long.MAX_VALUE, true)) {
                if (fileLock == null) {
                    return false;
                }
            }
            // file is present but no process is holding the lock
            return true;
        } catch (OverlappingFileLockException e) {
            // another process has the lock
            return false;
        } catch (NoSuchFileException e) {
            // the file doesn't exist, nobody has the lock
            return true;
        } catch (IOException e) {
            LOGGER.error(String.format("Exception occurred whilst trying to check lock on file %s : %s%n", file.getAbsolutePath(), e));
            return true; // maybe we should re-throw?
        }
    }

    @Override
    public boolean obtainLock(int id, String metaInfo) throws IOException {
        final File file = toFile(id);
        if (!isLockFree(file, id)) {
            return false;
        }

        FileChannel fc = FileChannel.open(file.toPath(), openOptions, fileAttr);
        FileLock fl = fc.tryLock();

        if (fl == null) {
            LOGGER.error(String.format("Could not obtain lock on file %s%n", file.getAbsolutePath()));
            return false;
        } else {
            LOGGER.debug(String.format("Obtained lock on file %s (%s)%n", file.getAbsolutePath(), metaInfo));
            locks[id] = new LockReference(fc, fl);

            byte[] content = String.format("%s%n%s", metaInfo, df.format(new Date())).getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(content);
            while (buffer.hasRemaining()) {
                fc.write(buffer);
            }
            return true;
        }
    }

    @Override
    public boolean releaseLock(int id) {
        LockReference lock = locks[id];
        if (lock == null) {
            LOGGER.error(String.format("Cannot release lock for id %d as don't have it!", id));
            return false;
        }

        try {
            locks[id] = null;
            lock.lock.release();
            lock.channel.close();
            return true;
        } catch (IOException e) {
            LOGGER.error(String.format("Couldn't release lock for id %d due to exception: %s%n", id, e.getMessage()));
            return false;
        }
    }

    @Override
    public String getMetaInfo(int id) throws IOException {
        final File file = toFile(id);
        if (isLockFree(file, id)) {
            LOGGER.warn("Cannot obtain lock on lock file {}", file.getAbsolutePath());
            return null;
        }

        LockReference lr = locks[id];
        if (lr != null) {
            return readMetaInfoFromLockFileChannel(file, lr.channel);
        } else {
            try (FileChannel fc = FileChannel.open(file.toPath(), READ)) {
                return readMetaInfoFromLockFileChannel(file, fc);
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
        try {
            if (file.exists() && OS.startsWith("linux")) {
                Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to set file permissions \"rwxrwxrwx\" for {} due to {}", file, e);
        }
        return file;
    }

    private File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }
}
