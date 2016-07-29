package net.openhft.affinity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static net.openhft.affinity.LockCheck.IS_LINUX;

/**
 * @author Rob Austin.
 */
public class LockCheckTest {

    private static final int PROCESS_ID = 2222;
    private static int CORE = 1111;

    @Before
    public void before() {
        System.setProperty("java.io.tmpdir", LockCheck.TMP + "/" + System.nanoTime());
    }

    @Test
    public void test() throws IOException {
        if (IS_LINUX) {
            LockCheck lockCheck = new LockCheck();
            Assert.assertTrue(lockCheck.isFreeCpu(CORE));
            Assert.assertEquals(LockCheck.getPID(), lockCheck.getProcessForCore(CORE));
        }
    }

    @Test
    public void testPidOnLinux() {
        final LockCheck lockCheck = new LockCheck();

        if (IS_LINUX)
            Assert.assertTrue(lockCheck.isProcessRunning(LockCheck.getPID()));

    }

    @Test
    public void testReplace() throws IOException {
        CORE++;
        if (IS_LINUX) {
            LockCheck lockCheck = new LockCheck();
            Assert.assertTrue(lockCheck.isFreeCpu(CORE + 1));
            lockCheck.replacePid(CORE, 123L);
            Assert.assertEquals(123L, lockCheck.getProcessForCore(CORE));
        }
    }


}
