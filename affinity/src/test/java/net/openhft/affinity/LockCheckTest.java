package net.openhft.affinity;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Rob Austin.
 */
public class LockCheckTest {

    private static final int CORE = 1111;
    private static final int PROCESS_ID = 2222;

    @Test
    public void test() throws IOException {
        LockCheck lockCheck = new LockCheck();
        lockCheck.isCoreAlreadyAssigned(CORE, PROCESS_ID);
        Assert.assertEquals(PROCESS_ID, lockCheck.getPid(CORE));
    }

    @Test
    public void testPidOnLinux() {
        final LockCheck lockCheck = new LockCheck();

        if (LockCheck.IS_LINUX)
            Assert.assertTrue(lockCheck.isProcessRunning(Affinity.getPID()));

    }
}
