package net.openhft.affinity.impl;

import junit.framework.TestCase;
import net.openhft.affinity.IAffinity;

import java.util.BitSet;

public class NullAffinityTest extends TestCase {

    private IAffinity affinity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        affinity = NullAffinity.INSTANCE;
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
        try {
            affinity.getThreadId();
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
            // Expected exception
        }
    }
}
