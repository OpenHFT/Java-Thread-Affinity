/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for improved error handling in the native layer.
 * Exercises the enhanced exception throwing and parameter validation.
 */
public class NativeAffinityErrorHandlingTest {

    @BeforeAll
    public static void checkNativeLibraryLoaded() {
        assumeTrue(NativeAffinity.LOADED, "Native library must be loaded for these tests");
    }

    @Test
    public void getAffinityHandlesErrorsGracefully() {
        // Should not throw - even if there are internal errors, should return null or valid BitSet
        BitSet affinity = NativeAffinity.INSTANCE.getAffinity();

        // Result should be either null or valid
        if (affinity != null) {
            // Should not be in an inconsistent state
            int length = affinity.length();
            assertTrue(length >= 0, "affinity length should be non-negative");
        }
    }

    @Test
    public void setAffinityWithEmptyBitSetHandlesGracefully() {
        assumeTrue(isLinux(), "requires Linux");

        BitSet original = NativeAffinity.INSTANCE.getAffinity();
        try {
            BitSet empty = new BitSet();

            // Should either succeed or throw RuntimeException (not crash)
            try {
                NativeAffinity.INSTANCE.setAffinity(empty);
            } catch (RuntimeException e) {
                // Expected on some systems
                String message = e.getMessage();
                assertTrue(message == null || !message.isEmpty(), "empty BitSet exception message");
            }
            int cpu = NativeAffinity.INSTANCE.getCpu();
            assertTrue(cpu >= -1, "cpu should be valid after setAffinity(empty)");
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
        assumeTrue(isLinux(), "requires Linux");

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
                assertNotNull(e.getMessage(), "exception should have message");
            }
            int cpu = NativeAffinity.INSTANCE.getCpu();
            assertTrue(cpu >= -1, "cpu should be valid after setAffinity(large)");
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
            assertTrue(processId > 0, "process ID should be positive on Linux: " + processId);

            // Should match system PID
            String javaPid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            int expectedPid = Integer.parseInt(javaPid);
            assertEquals(expectedPid, processId, "process ID should match Java runtime PID");
        } else {
            // On non-Linux, should return -1 or throw UnsupportedOperationException
            assertEquals(-1, processId, "process ID should be -1 on non-Linux platforms");
        }
    }

    @Test
    public void getThreadIdReturnsValidValue() {
        int threadId = NativeAffinity.INSTANCE.getThreadId();

        if (isLinux()) {
            // On Linux, should return a valid thread ID (positive integer)
            assertTrue(threadId > 0, "thread ID should be positive on Linux: " + threadId);
        } else {
            // On non-Linux, should return -1
            assertEquals(-1, threadId, "thread ID should be -1 on non-Linux platforms");
        }
    }

    @Test
    public void getCpuReturnsValidValue() {
        int cpu = NativeAffinity.INSTANCE.getCpu();

        if (isLinux()) {
            // Should return a valid CPU ID (0 to number of CPUs - 1)
            int numCpus = Runtime.getRuntime().availableProcessors();
            assertTrue(cpu >= 0, "CPU ID should be non-negative: " + cpu);
            assertTrue(cpu < numCpus, "CPU ID should be less than number of CPUs: " + cpu + " < " + numCpus);
        } else {
            // On non-Linux, should return -1
            assertEquals(-1, cpu, "CPU ID should be -1 on non-Linux platforms");
        }
    }

    @Test
    public void multipleGetAffinityCallsAreConsistent() {
        // Multiple calls should not cause memory corruption or crashes
        BitSet affinity1 = NativeAffinity.INSTANCE.getAffinity();
        BitSet affinity2 = NativeAffinity.INSTANCE.getAffinity();
        BitSet affinity3 = NativeAffinity.INSTANCE.getAffinity();

        // All should be valid
        assertNotNull(affinity1, "first call should return valid result");
        assertNotNull(affinity2, "second call should return valid result");
        assertNotNull(affinity3, "third call should return valid result");

        // Should be equal (assuming no other thread changed affinity)
        assertEquals(affinity1, affinity2, "affinity should be consistent");
        assertEquals(affinity2, affinity3, "affinity should be consistent");
    }

    @Test
    public void concurrentAccessDoesNotCrash() throws InterruptedException {
        // Test thread safety of native calls
        final int threadCount = 5;
        final int iterationsPerThread = 10;
        Thread[] threads = new Thread[threadCount];
        final Throwable[] failures = new Throwable[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        // Mix of different operations
                        BitSet affinity = NativeAffinity.INSTANCE.getAffinity();
                        assertNotNull(affinity, "affinity should not be null");

                        int cpu = NativeAffinity.INSTANCE.getCpu();
                        assertTrue(cpu >= -1, "CPU should be valid");

                        int pid = NativeAffinity.INSTANCE.getProcessId();
                        assertTrue(pid >= -1, "PID should be valid");

                        int tid = NativeAffinity.INSTANCE.getThreadId();
                        assertTrue(tid >= -1, "TID should be valid");
                    }
                } catch (Throwable t) {
                    failures[threadIndex] = t;
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
            if (failures[i] != null) {
                fail("Thread " + i + " failed: " + failures[i]);
            }
        }
    }

    @Test
    public void exceptionMessagesAreInformative() {
        assumeTrue(isLinux(), "requires Linux");

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
                assertNotNull(message, "exception should have a message");
                assertFalse(message.isEmpty(), "exception message should not be empty");

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
            assertNotNull(affinity, "affinity should be valid on iteration " + i);

            @SuppressWarnings("unused")
            int cpu = NativeAffinity.INSTANCE.getCpu();
            @SuppressWarnings("unused")
            int pid = NativeAffinity.INSTANCE.getProcessId();
            @SuppressWarnings("unused")
            int tid = NativeAffinity.INSTANCE.getThreadId();
        }
    }

    private boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
}
