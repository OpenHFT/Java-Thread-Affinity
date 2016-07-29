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
    private static int CPU = 1111;

    @Before
    public void before() {
        System.setProperty("java.io.tmpdir", LockCheck.TMP + "/" + System.nanoTime());
    }

    @Test
    public void test() throws IOException {
        if (IS_LINUX) {

            Assert.assertTrue(LockCheck.isCpuFree(CPU));
            LockCheck.updateCpu(CPU);
            Assert.assertEquals(LockCheck.getPID(), LockCheck.getProcessForCpu(CPU));
        }
    }

    @Test
    public void testPidOnLinux() {


        if (IS_LINUX)
            Assert.assertTrue(LockCheck.isProcessRunning(LockCheck.getPID()));

    }

    @Test
    public void testReplace() throws IOException {
        CPU++;
        if (IS_LINUX) {

            Assert.assertTrue(LockCheck.isCpuFree(CPU + 1));
            LockCheck.replacePid(CPU, 123L);
            Assert.assertEquals(123L, LockCheck.getProcessForCpu(CPU));
        }
    }


}
