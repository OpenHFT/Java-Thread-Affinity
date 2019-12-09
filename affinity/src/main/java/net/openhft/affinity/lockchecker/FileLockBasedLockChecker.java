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
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardOpenOption.*;
import static net.openhft.affinity.impl.VanillaCpuLayout.MAX_CPUS_SUPPORTED;

public class FileLockBasedLockChecker extends FileBasedLockChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLockBasedLockChecker.class);
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final LockChecker instance = new FileLockBasedLockChecker();
    private static final HashSet<StandardOpenOption> openOptions = new HashSet<>(Arrays.asList(CREATE_NEW, WRITE, READ, SYNC));
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
        //if no file exists - nobody has the lock for sure
        if (!file.exists()) {
            return true;
        }

        //do we have the lock already?
        LockReference existingLock = locks[id];
        if (existingLock != null) {
            return false;
        }

        //does another process have the lock?
        try {
            FileChannel fc = FileChannel.open(file.toPath(), WRITE);
            FileLock fileLock = fc.tryLock();
            if (fileLock == null) {
                return false;
            }
        } catch (IOException | OverlappingFileLockException e) {
            LOGGER.error(String.format("Exception occurred whilst trying to check lock on file %s : %s%n", file.getAbsolutePath(), e));
        }

        //file is present but nobody has it locked - delete it
        LOGGER.info(String.format("Deleting %s as nobody has the lock", file.getAbsolutePath()));
        file.delete();

        return true;
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
            toFile(id).delete();
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
        if (lr == null) {
            return null;
        }
        FileChannel fc = lr.channel;
        ByteBuffer buffer = ByteBuffer.allocate(64);
        int len = fc.read(buffer, 0);
        String content = len < 1 ? "" : new String(buffer.array(), 0, len);
        if (content.isEmpty()) {
            LOGGER.warn("Empty lock file {}", file.getAbsolutePath());
            return null;
        }
        return content.substring(0, content.indexOf("\n"));
    }

    @NotNull
    @Override
    protected File toFile(int id) {
        File file = super.toFile(id);
        try {
            if (file.exists() && OS.startsWith("linux")) {
                Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to set file permissions \"rwxrwxrwx\" for {} due to {}", file.toString(), e);
        }
        return file;
    }
}
