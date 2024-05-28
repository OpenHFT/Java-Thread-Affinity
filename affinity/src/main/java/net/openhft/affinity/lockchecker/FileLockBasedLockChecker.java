/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
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

    private static final int MAX_LOCK_RETRIES = 5;
    private static final ThreadLocal<SimpleDateFormat> dfTL = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z"));
    private static final FileAttribute<Set<PosixFilePermission>> LOCK_FILE_ATTRIBUTES = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw-rw-"));
    private static final Set<OpenOption> LOCK_FILE_OPEN_OPTIONS = new HashSet<>(Arrays.asList(READ, WRITE, CREATE, SYNC));
    private static final Logger LOGGER = LoggerFactory.getLogger(FileLockBasedLockChecker.class);
    private static final FileLockBasedLockChecker instance = new FileLockBasedLockChecker();
    private final LockReference[] locks = new LockReference[MAX_CPUS_SUPPORTED];

    protected FileLockBasedLockChecker() {
        //nothing
    }

    public static LockChecker getInstance() {
        return instance;
    }

    @Override
    public synchronized boolean isLockFree(int id) {
        // check if this process already has the lock
        if (locks[id] != null) {
            return false;
        }

        // check if another process has the lock
        File lockFile = toFile(id);
        try (final FileChannel channel = FileChannel.open(lockFile.toPath(), READ)) {
            // if we can acquire a shared lock, nobody has an exclusive lock
            try (final FileLock fileLock = channel.tryLock(0, Long.MAX_VALUE, true)) {
                if (fileLock != null && fileLock.isValid()) {
                    if (!lockFile.delete()) { // try and clean up the orphaned lock file
                        LOGGER.debug("Couldn't delete orphaned lock file " + lockFile);
                    }
                    return true;
                } else {
                    // another process has an exclusive lock
                    return false;
                }
            } catch (OverlappingFileLockException e) {
                // someone else (in the same JVM) has an exclusive lock
                /*
                 * This shouldn't happen under normal circumstances, we have the singleton
                 * {@link #locks} array to prevent overlapping locks from the same JVM, but
                 * it can occur when there are multiple classloaders in the JVM
                 */
                return false;
            }
        } catch (NoSuchFileException e) {
            // no lock file exists, nobody has the lock
            return true;
        } catch (IOException e) {
            LOGGER.warn("An unexpected error occurred checking if the lock was free, assuming it's not", e);
            return false;
        }
    }

    @Override
    public synchronized boolean obtainLock(int id, String metaInfo) throws IOException {
        int attempt = 0;
        while (attempt < MAX_LOCK_RETRIES) {
            try {
                LockReference lockReference = tryAcquireLockOnFile(id, metaInfo);
                if (lockReference != null) {
                    locks[id] = lockReference;
                    return true;
                }
                return false;
            } catch (ConcurrentLockFileDeletionException e) {
                attempt++;
            }
        }
        LOGGER.warn("Exceeded maximum retries for locking CPU " + id + ", failing acquire");
        return false;
    }

    /**
     * Attempts to acquire an exclusive lock on the core lock file.
     * <p>
     * It will fail if another process already has an exclusive lock on the file.
     *
     * @param id       The CPU ID to acquire
     * @param metaInfo The meta-info to write to the file upon successful acquisition
     * @return The {@link LockReference} if the lock was successfully acquired, null otherwise
     * @throws IOException                         If an IOException occurs creating or writing to the file
     * @throws ConcurrentLockFileDeletionException If another process deleted the file between us opening it and locking it
     */
    private LockReference tryAcquireLockOnFile(int id, String metaInfo) throws IOException, ConcurrentLockFileDeletionException {
        final File lockFile = toFile(id);
        final FileChannel fileChannel = FileChannel.open(lockFile.toPath(), LOCK_FILE_OPEN_OPTIONS, LOCK_FILE_ATTRIBUTES); // NOSONAR
        try {
            final FileLock fileLock = fileChannel.tryLock(0, Long.MAX_VALUE, false);
            if (fileLock == null) {
                // someone else has a lock (exclusive or shared), fail to acquire
                closeQuietly(fileChannel);
                return null;
            } else {
                if (!lockFile.exists()) {
                    // someone deleted the file between us opening it and acquiring the lock, signal to retry
                    closeQuietly(fileLock, fileChannel);
                    throw new ConcurrentLockFileDeletionException();
                } else {
                    // we have the lock, the file exists. That's success.
                    writeMetaInfoToFile(fileChannel, metaInfo);
                    return new LockReference(fileChannel, fileLock);
                }
            }
        } catch (OverlappingFileLockException e) {
            // someone else (in the same JVM) has a lock, fail to acquire
            /*
             * This shouldn't happen under normal circumstances, we have the singleton
             * {@link #locks} array to prevent overlapping locks from the same JVM, but
             * it can occur when there are multiple classloaders in the JVM
             */
            closeQuietly(fileChannel);
            return null;
        }
    }

    private void writeMetaInfoToFile(FileChannel fc, String metaInfo) throws IOException {
        byte[] content = String.format("%s%n%s", metaInfo, dfTL.get().format(new Date())).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(content);
        while (buffer.hasRemaining()) {
            fc.write(buffer);
        }
    }

    @Override
    public synchronized boolean releaseLock(int id) {
        if (locks[id] != null) {
            final File lockFile = toFile(id);
            if (!lockFile.delete()) {
                LOGGER.warn("Couldn't delete lock file on release: " + lockFile);
            }
            closeQuietly(locks[id].lock, locks[id].channel);
            locks[id] = null;
            return true;
        }
        return false;
    }

    private void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                LOGGER.warn("Error closing " + closeable.getClass().getName(), e);
            }
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
        return new File(tmpDir(), "cpu-" + id + ".lock");
    }

    private File tmpDir() {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));

        if (!tempDir.exists())
            tempDir.mkdirs();

        return tempDir;
    }

    /**
     * Thrown when another process deleted the lock file between us opening the file and acquiring the lock
     */
    class ConcurrentLockFileDeletionException extends Exception {
        private static final long serialVersionUID = 0L;
    }
}
