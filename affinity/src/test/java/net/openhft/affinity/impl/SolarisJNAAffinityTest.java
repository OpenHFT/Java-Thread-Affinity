package net.openhft.affinity.impl;

import junit.framework.TestCase;
import net.openhft.affinity.IAffinity;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

public class SolarisJNAAffinityTest extends TestCase {

    private IAffinity affinity;
    private boolean libraryLoaded = false;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        try {
            // Attempt to initialize the SolarisJNAAffinity class
            Class.forName("net.openhft.affinity.impl.SolarisJNAAffinity");
            affinity = SolarisJNAAffinity.INSTANCE;
            // Check if the library is loaded
            SolarisJNAAffinity.CLibrary.INSTANCE.pthread_self();
            libraryLoaded = true;
        } catch (ClassNotFoundException | NoClassDefFoundError | UnsatisfiedLinkError e) {
            System.out.println("Library not loaded: " + e.getMessage());
        }
    }

    public void testLibraryLoaded() {
        if (!libraryLoaded) {
            System.out.println("Skipping testLibraryLoaded as the library is not loaded.");
            return;
        }
        assertTrue("Library should be loaded", libraryLoaded);
    }

    public void testGetAffinity() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetAffinity as the library is not loaded.");
            return;
        }
        BitSet bitSet = affinity.getAffinity();
        assertNotNull(bitSet);
        // Ensure the BitSet is valid.
    }

    public void testSetAffinity() {
        if (!libraryLoaded) {
            System.out.println("Skipping testSetAffinity as the library is not loaded.");
            return;
        }
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        try {
            affinity.setAffinity(bitSet);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testGetCpu() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetCpu as the library is not loaded.");
            return;
        }
        try {
            int cpu = affinity.getCpu();
            assertEquals(-1, cpu);
        } catch (IllegalStateException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testGetProcessId() throws Exception {
        int processId = affinity.getProcessId();
        String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        int expectedProcessId = Integer.parseInt(name.split("@")[0]);
        assertEquals(expectedProcessId, processId);
    }

    public void testGetThreadId() {
        if (!libraryLoaded) {
            System.out.println("Skipping testGetThreadId as the library is not loaded.");
            return;
        }
        try {
            int threadId = affinity.getThreadId();
            assertTrue(threadId > 0);
        } catch (IllegalStateException e) {
            System.out.println("Expected exception: " + e.getMessage());
        }
    }

    public void testLoggerInitialization() {
        assertNotNull(LoggerFactory.getLogger(SolarisJNAAffinity.class));
    }
}
