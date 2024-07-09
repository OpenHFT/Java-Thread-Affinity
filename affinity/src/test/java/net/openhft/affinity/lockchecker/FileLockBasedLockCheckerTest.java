package net.openhft.affinity.lockchecker;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileLockBasedLockCheckerTest extends TestCase {

    private FileLockBasedLockChecker lockChecker;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int TEST_CPU_ID = 0;
    private static final String META_INFO = "test_meta_info";
    private File lockFile;
    private int testId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lockChecker = (FileLockBasedLockChecker) FileLockBasedLockChecker.getInstance();
        lockFile = new File(TEMP_DIR, "cpu-" + testId + ".lock");
        if (lockFile.exists()) {
            Files.delete(lockFile.toPath());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (lockFile.exists()) {
            Files.delete(lockFile.toPath());
        }
        lockChecker.releaseLock(testId);
    }

    public void testGetInstance() {
        assertNotNull(FileLockBasedLockChecker.getInstance());
    }

    public void testIsLockFree_NoLockFileExists() throws IOException {
        assertFalse(lockFile.exists());
        boolean isLockFree = lockChecker.isLockFree(testId);
        assertTrue(isLockFree);
    }

    public void testIsLockFree_LockFileExists() throws IOException {
        if (!lockFile.exists()) {
            lockFile.createNewFile();
        }

        try (FileChannel channel = FileChannel.open(lockFile.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            try (java.nio.channels.FileLock lock = channel.lock()) {
                boolean isLockFree = lockChecker.isLockFree(testId);
                assertFalse(isLockFree);
            }
        } finally {
            Files.delete(lockFile.toPath());
        }
    }

    public void testReadMetaInfoFromLockFileChannel() throws Exception {
        FileChannel fileChannel = null;
        try {
            File file = lockChecker.toFile(TEST_CPU_ID);
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            ByteBuffer buffer = ByteBuffer.wrap((META_INFO + "\n").getBytes());
            while (buffer.hasRemaining()) {
                fileChannel.write(buffer);
            }
            fileChannel.close();

            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            Method readMetaInfoFromLockFileChannelMethod = FileLockBasedLockChecker.class.getDeclaredMethod("readMetaInfoFromLockFileChannel", File.class, FileChannel.class);
            readMetaInfoFromLockFileChannelMethod.setAccessible(true);
            String metaInfo = (String) readMetaInfoFromLockFileChannelMethod.invoke(lockChecker, file, fileChannel);
            assertEquals(META_INFO, metaInfo);
        } finally {
            if (fileChannel != null) {
                fileChannel.close();
            }
        }
    }

    public void testToFile() {
        File file = lockChecker.toFile(TEST_CPU_ID);
        assertEquals(new File(System.getProperty("java.io.tmpdir"), "cpu-" + TEST_CPU_ID + ".lock"), file);
    }

    public void testTmpDir() throws Exception {
        Method tmpDirMethod = FileLockBasedLockChecker.class.getDeclaredMethod("tmpDir");
        tmpDirMethod.setAccessible(true);
        File tmpDir = (File) tmpDirMethod.invoke(lockChecker);
        assertEquals(new File(System.getProperty("java.io.tmpdir")), tmpDir);
    }
}
