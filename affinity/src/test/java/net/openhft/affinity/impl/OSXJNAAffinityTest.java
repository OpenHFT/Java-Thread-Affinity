package net.openhft.affinity.impl;

import junit.framework.TestCase;
import net.openhft.affinity.IAffinity;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

public class OSXJNAAffinityTest extends TestCase {

    private IAffinity affinity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        affinity = OSXJNAAffinity.INSTANCE;
    }

    public void testGetAffinity() {
        BitSet bitSet = affinity.getAffinity();
        assertNotNull(bitSet);
        assertEquals(0, bitSet.length());
    }

    public void testSetAffinity() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        affinity.setAffinity(bitSet);
        // Since this is a no-op, we can't assert any changes. Just ensure no exceptions are thrown.
    }

    public void testGetCpu() {
        int cpu = affinity.getCpu();
        assertEquals(-1, cpu);
    }

    public void testGetProcessId() {
        int processId = affinity.getProcessId();
        String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        int expectedProcessId = Integer.parseInt(name.split("@")[0]);
        assertEquals(expectedProcessId, processId);
    }

    public void testGetThreadId() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            int threadId = affinity.getThreadId();
            assertTrue(threadId > 0);
        } else {
            System.out.println("Skipping testGetThreadId on non-macOS systems.");
        }
    }

    public void testLoggerInitialization() {
        assertNotNull(LoggerFactory.getLogger(OSXJNAAffinity.class));
    }
}
