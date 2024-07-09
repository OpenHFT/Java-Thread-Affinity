package software.chronicle.enterprise.internals.impl;

import junit.framework.TestCase;
import java.util.BitSet;

public class NativeAffinityTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Check if the native library is loaded
        if (!NativeAffinity.LOADED) {
            fail("Native library is not loaded. Skipping tests.");
        }
    }

    public void testGetAffinity() {
        if (NativeAffinity.LOADED) {
            BitSet affinity = NativeAffinity.INSTANCE.getAffinity();
            assertNotNull("Affinity should not be null", affinity);
        }
    }

    public void testSetAffinity() {
        if (NativeAffinity.LOADED) {
            BitSet affinity = new BitSet();
            affinity.set(0); // Set the affinity to the first CPU
            NativeAffinity.INSTANCE.setAffinity(affinity);

            BitSet retrievedAffinity = NativeAffinity.INSTANCE.getAffinity();
            assertEquals("The set and retrieved affinity should be the same", affinity, retrievedAffinity);
        }
    }

    public void testGetCpu() {
        if (NativeAffinity.LOADED) {
            int cpu = NativeAffinity.INSTANCE.getCpu();
            assertTrue("CPU ID should be non-negative", cpu >= 0);
        }
    }

    public void testGetProcessId() {
        if (NativeAffinity.LOADED) {
            int processId = NativeAffinity.INSTANCE.getProcessId();
            assertTrue("Process ID should be positive", processId > 0);
        }
    }

    public void testGetThreadId() {
        if (NativeAffinity.LOADED) {
            int threadId = NativeAffinity.INSTANCE.getThreadId();
            assertTrue("Thread ID should be positive", threadId > 0);
        }
    }

    public void testLoaded() {
        assertTrue("Native library should be loaded", NativeAffinity.LOADED);
    }
}
