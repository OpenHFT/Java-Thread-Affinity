package net.openhft.affinity;

import net.openhft.affinity.lockchecker.FileLockBasedLockChecker;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class BaseAffinityTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private String originalTmpDir;

    @Before
    public void setTmpDirectory() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", folder.getRoot().getAbsolutePath());
    }

    @After
    public void restoreTmpDirectoryAndReleaseAllLocks() {
        // don't leave any locks locked
        for (int i = 0; i < AffinityLock.PROCESSORS; i++) {
            if (!FileLockBasedLockChecker.getInstance().isLockFree(i)) {
                FileLockBasedLockChecker.getInstance().releaseLock(i);
            }
        }
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }
}
