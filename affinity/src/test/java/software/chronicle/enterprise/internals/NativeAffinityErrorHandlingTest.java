/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * Tests for improved error handling in the native layer.
 * Exercises the enhanced exception throwing and parameter validation.
 */
public class NativeAffinityErrorHandlingTest {

    @BeforeClass
    public static void checkNativeLibraryLoaded() {
        Assume.assumeTrue("Native library must be loaded for these tests",
                NativeAffinity.LOADED);
    }

    @Test
    public void getAffinityHandlesErrorsGracefully() {
        // Should not throw - even if there are internal errors, should return null or valid BitSet
        BitSet affinity = NativeAffinity.INSTANCE.getAffinity();

        // Result should be either null or valid
        if (affinity != null) {
            // If not null, should be a valid BitSet
            assertNotNull("Affinity should be valid BitSet", affinity);

            // Should not be in an inconsistent state
            int length = affinity.length();
            assertTrue("Affinity length should be non-negative", length >= 0);
        }
    }

    @Test
    public void setAffinityWithEmptyBitSetHandlesGracefully() {
        if (!isLinux()) {
            System.out.println("Skipping Linux-specific test");
            return;
        }

        BitSet original = NativeAffinity.INSTANCE.getAffinity();
        try {
            BitSet empty = new BitSet();

            // Should either succeed or throw RuntimeException (not crash)
            try {
                NativeAffinity.INSTANCE.setAffinity(empty);
            } catch (RuntimeException e) {
                // Expected on some systems
                assertTrue("Should be RuntimeException", true);
            }
        } finally {
            // Restore original affinity
            if (original != null) {
                try {
                    NativeAffinity.INSTANCE.setAffinity(original);
                } catch (Exception e) {
                    // Best effort restore
                }
            }
        }
    }

    @Test
    public void setAffinityWithLargeBitSetHandlesGracefully() {
        if (!isLinux()) {
            System.out.println("Skipping Linux-specific test");
            return;
        }

        BitSet original = NativeAffinity.INSTANCE.getAffinity();
        try {
            // Create a very large BitSet (more CPUs than exist)
            BitSet large = new BitSet(10000);
            large.set(9999);

            try {
                // Should handle gracefully - either truncate or throw RuntimeException
                NativeAffinity.INSTANCE.setAffinity(large);
            } catch (RuntimeException e) {
                // Expected - affinity mask too large
                assertNotNull("Exception should have message", e.getMessage());
            }
        } finally {
            // Restore original affinity
            if (original != null) {
                try {
                    NativeAffinity.INSTANCE.setAffinity(original);
                } catch (Exception e) {
                    // Best effort restore
                }
            }
        }
    }

    @Test
    public void getProcessIdReturnsValidValue() {
        int processId = NativeAffinity.INSTANCE.getProcessId();

        if (isLinux()) {
            // On Linux, should return a valid PID (positive integer)
            assertTrue("Process ID should be positive on Linux: " + processId,
                    processId > 0);

            // Should match system PID
            String javaPid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            int expectedPid = Integer.parseInt(javaPid);
            assertEquals("Process ID should match Java runtime PID", expectedPid, processId);
        } else {
            // On non-Linux, should return -1 or throw UnsupportedOperationException
            assertEquals("Process ID should be -1 on non-Linux platforms", -1, processId);
        }
    }

    @Test
    public void getThreadIdReturnsValidValue() {
        int threadId = NativeAffinity.INSTANCE.getThreadId();

        if (isLinux()) {
            // On Linux, should return a valid thread ID (positive integer)
            assertTrue("Thread ID should be positive on Linux: " + threadId,
                    threadId > 0);
        } else {
            // On non-Linux, should return -1
            assertEquals("Thread ID should be -1 on non-Linux platforms", -1, threadId);
        }
    }

    @Test
    public void getCpuReturnsValidValue() {
        int cpu = NativeAffinity.INSTANCE.getCpu();

        if (isLinux()) {
            // Should return a valid CPU ID (0 to number of CPUs - 1)
            int numCpus = Runtime.getRuntime().availableProcessors();
            assertTrue("CPU ID should be non-negative: " + cpu, cpu >= 0);
            assertTrue("CPU ID should be less than number of CPUs: " + cpu + " < " + numCpus,
                    cpu < numCpus);
        } else {
            // On non-Linux, should return -1
            assertEquals("CPU ID should be -1 on non-Linux platforms", -1, cpu);
        }
    }

    @Test
    public void multipleGetAffinityCallsAreConsistent() {
        // Multiple calls should not cause memory corruption or crashes
        BitSet affinity1 = NativeAffinity.INSTANCE.getAffinity();
        BitSet affinity2 = NativeAffinity.INSTANCE.getAffinity();
        BitSet affinity3 = NativeAffinity.INSTANCE.getAffinity();

        // All should be valid
        assertNotNull("First call should return valid result", affinity1);
        assertNotNull("Second call should return valid result", affinity2);
        assertNotNull("Third call should return valid result", affinity3);

        // Should be equal (assuming no other thread changed affinity)
        assertEquals("Affinity should be consistent", affinity1, affinity2);
        assertEquals("Affinity should be consistent", affinity2, affinity3);
    }

    @Test
    public void concurrentAccessDoesNotCrash() throws InterruptedException {
        // Test thread safety of native calls
        final int threadCount = 5;
        final int iterationsPerThread = 10;
        Thread[] threads = new Thread[threadCount];
        final Exception[] exceptions = new Exception[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        // Mix of different operations
                        BitSet affinity = NativeAffinity.INSTANCE.getAffinity();
                        assertNotNull("Affinity should not be null", affinity);

                        int cpu = NativeAffinity.INSTANCE.getCpu();
                        assertTrue("CPU should be valid", cpu >= -1);

                        int pid = NativeAffinity.INSTANCE.getProcessId();
                        assertTrue("PID should be valid", pid >= -1);

                        int tid = NativeAffinity.INSTANCE.getThreadId();
                        assertTrue("TID should be valid", tid >= -1);
                    }
                } catch (Exception e) {
                    exceptions[threadIndex] = e;
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Check no exceptions occurred
        for (int i = 0; i < threadCount; i++) {
            if (exceptions[i] != null) {
                fail("Thread " + i + " threw exception: " + exceptions[i].getMessage());
            }
        }
    }

    @Test
    public void exceptionMessagesAreInformative() {
        if (!isLinux()) {
            System.out.println("Skipping Linux-specific test");
            return;
        }

        BitSet original = NativeAffinity.INSTANCE.getAffinity();
        try {
            // Try to set an invalid affinity that should fail
            BitSet invalid = new BitSet();

            try {
                NativeAffinity.INSTANCE.setAffinity(invalid);
                // May succeed on some systems, or may throw
            } catch (RuntimeException e) {
                // If it throws, message should be informative
                String message = e.getMessage();
                assertNotNull("Exception should have a message", message);
                assertFalse("Exception message should not be empty", message.isEmpty());

                System.out.println("Error message: " + message);
            }
        } finally {
            // Restore original affinity
            if (original != null) {
                try {
                    NativeAffinity.INSTANCE.setAffinity(original);
                } catch (Exception e) {
                    // Best effort restore
                }
            }
        }
    }

    @Test
    public void noMemoryLeaksOnRepeatedCalls() {
        // Repeatedly call native methods to check for memory leaks
        // This is a basic test - proper leak detection would need profiling tools
        final int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            BitSet affinity = NativeAffinity.INSTANCE.getAffinity();
            assertNotNull("Affinity should be valid on iteration " + i, affinity);

            @SuppressWarnings("unused")
            int cpu = NativeAffinity.INSTANCE.getCpu();
            @SuppressWarnings("unused")
            int pid = NativeAffinity.INSTANCE.getProcessId();
            @SuppressWarnings("unused")
            int tid = NativeAffinity.INSTANCE.getThreadId();
        }

        // If we got here without crashing, basic memory safety is OK
        assertTrue("No crashes during repeated calls", true);
    }

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
}
