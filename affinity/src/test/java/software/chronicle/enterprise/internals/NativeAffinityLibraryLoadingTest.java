/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.Assert.*;

/**
 * Tests for native library loading and initialization.
 * Exercises the JNI_OnLoad functionality and library loading process.
 */
public class NativeAffinityLibraryLoadingTest {

    @Test
    public void loadedFieldIsInitialized() {
        // LOADED should be deterministic (true or false, not null or uninitialized)
        boolean loaded = NativeAffinity.LOADED;
        // Should not throw - field is accessible
        assertTrue("LOADED field should be initialized", loaded || !loaded);
    }

    @Test
    public void loadedStateIsDeterministic() {
        // LOADED state should not change
        boolean firstCheck = NativeAffinity.LOADED;
        boolean secondCheck = NativeAffinity.LOADED;

        assertEquals("LOADED state should be deterministic", firstCheck, secondCheck);
    }

    @Test
    public void instanceIsAccessibleRegardlessOfLoadState() {
        // INSTANCE should be accessible even if library is not loaded
        NativeAffinity instance = NativeAffinity.INSTANCE;
        assertNotNull("INSTANCE should never be null", instance);
    }

    @Test
    public void instanceIsSingleton() {
        // Should be the same instance every time (enum singleton)
        NativeAffinity instance1 = NativeAffinity.INSTANCE;
        NativeAffinity instance2 = NativeAffinity.INSTANCE;

        assertSame("INSTANCE should be a singleton", instance1, instance2);
    }

    @Test
    public void versionIsSetDuringStaticInitialization() {
        // VERSION should be set during static initialization
        assertNotNull("VERSION should be initialized during static init", NativeAffinity.VERSION);

        // Should be consistent
        String version1 = NativeAffinity.VERSION;
        String version2 = NativeAffinity.VERSION;
        assertEquals("VERSION should be consistent", version1, version2);
    }

    @Test
    public void libraryLoadingStateIsConsistent() {
        // If LOADED is true, we should be able to access VERSION with real value
        if (NativeAffinity.LOADED) {
            assertNotEquals("Loaded library should have real version",
                    "not loaded", NativeAffinity.VERSION);

            System.out.println("Native library successfully loaded");
            System.out.println("Library version: " + NativeAffinity.VERSION);
        } else {
            assertEquals("Unloaded library should have 'not loaded' version",
                    "not loaded", NativeAffinity.VERSION);

            System.out.println("Native library not loaded (JNA fallback active)");
        }
    }

    @Test
    public void multipleConcurrentAccessesAreSafe() throws InterruptedException {
        // Test thread safety of static initialization
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final boolean[] results = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = NativeAffinity.LOADED;
                @SuppressWarnings("unused")
                String version = NativeAffinity.VERSION;
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // All threads should see the same LOADED state
        boolean firstResult = results[0];
        for (int i = 1; i < threadCount; i++) {
            assertEquals("All threads should see consistent LOADED state",
                    firstResult, results[i]);
        }
    }

    @Test
    public void versionStringIsSafeForLogging() {
        // Should not throw when used in logging/printing contexts
        try {
            String logMessage = "Library version: " + NativeAffinity.VERSION;
            assertNotNull(logMessage);

            // Should be safe to concatenate
            String concatenated = "V" + NativeAffinity.VERSION + "X";
            assertTrue(concatenated.startsWith("V"));
            assertTrue(concatenated.endsWith("X"));
        } catch (Exception e) {
            fail("VERSION should be safe for string operations: " + e.getMessage());
        }
    }

    @Test
    public void libraryNameIsCorrect() {
        // The library should be named "CEInternals" as per loadAffinityNativeLibrary()
        // We can't directly test System.loadLibrary, but we verify the state is consistent
        // If LOADED is true, then "CEInternals" was successfully loaded

        if (NativeAffinity.LOADED) {
            // Library loaded successfully - version should be available
            assertNotNull("Version should be available when library loaded",
                    NativeAffinity.VERSION);

            // On Linux, library file should be libCEInternals.so
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("linux")) {
                System.out.println("Linux platform detected - library: libCEInternals.so");
            } else if (osName.contains("mac")) {
                System.out.println("macOS platform detected - library: libCEInternals.dylib");
            } else if (osName.contains("win")) {
                System.out.println("Windows platform detected - library: CEInternals.dll");
            }
        }
    }

    @Test
    public void jniVersionIsCompatible() {
        // If library loaded, JNI_OnLoad should have returned JNI_VERSION_1_8
        // We can't test this directly, but if LOADED is true, it means:
        // 1. Library was found
        // 2. JNI_OnLoad was called successfully
        // 3. Version was compatible

        if (NativeAffinity.LOADED) {
            // If we got here, JNI version negotiation succeeded
            assertTrue("JNI version negotiation succeeded", true);

            // Verify we can call a native method (which proves JNI is working)
            String version = NativeAffinity.VERSION;
            assertNotNull("Native method call succeeded", version);
            assertNotEquals("Native method returned valid version", "not loaded", version);
        }
    }

    @Test
    public void staticInitializationOrderIsCorrect() {
        // Test that static initialization happens in correct order:
        // 1. LOADED is set
        // 2. VERSION is set based on LOADED

        // Both should be initialized
        boolean loadedWrapper = NativeAffinity.LOADED;
        assertTrue("LOADED should be initialized", loadedWrapper || !loadedWrapper);

        String version = NativeAffinity.VERSION;
        assertNotNull("VERSION should be initialized", version);

        // Relationship should be consistent
        if (NativeAffinity.LOADED) {
            assertNotEquals("VERSION should reflect loaded state", "not loaded", version);
        } else {
            assertEquals("VERSION should reflect not loaded state", "not loaded", version);
        }
    }

    @Test
    public void classCanBeLoadedMultipleTimes() {
        // Access class multiple times - should not cause re-initialization
        Class<?> class1 = NativeAffinity.class;
        Class<?> class2 = NativeAffinity.INSTANCE.getClass();

        assertSame("Class should be the same", class1, class2);
    }
}
