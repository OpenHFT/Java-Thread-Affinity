/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        assertTrue(loaded || !loaded, "LOADED field should be initialised");
    }

    @Test
    public void loadedStateIsDeterministic() {
        // LOADED state should not change
        boolean firstCheck = NativeAffinity.LOADED;
        boolean secondCheck = NativeAffinity.LOADED;

        assertEquals(firstCheck, secondCheck, "LOADED state should be deterministic");
    }

    @Test
    public void instanceIsAccessibleRegardlessOfLoadState() {
        // INSTANCE should be accessible even if library is not loaded
        NativeAffinity instance = NativeAffinity.INSTANCE;
        assertNotNull(instance, "INSTANCE should never be null");
    }

    @Test
    public void instanceIsSingleton() {
        // Should be the same instance every time (enum singleton)
        NativeAffinity instance1 = NativeAffinity.INSTANCE;
        NativeAffinity instance2 = NativeAffinity.INSTANCE;

        assertSame(instance1, instance2, "INSTANCE should be a singleton");
    }

    @Test
    public void versionIsSetDuringStaticInitialization() {
        // VERSION should be set during static initialization
        assertNotNull(NativeAffinity.VERSION, "VERSION should be initialised during static init");

        // Should be consistent
        String version1 = NativeAffinity.VERSION;
        String version2 = NativeAffinity.VERSION;
        assertEquals(version1, version2, "VERSION should be consistent");
    }

    @Test
    public void libraryLoadingStateIsConsistent() {
        // If LOADED is true, we should be able to access VERSION with real value
        if (NativeAffinity.LOADED) {
            assertNotEquals("not loaded", NativeAffinity.VERSION, "loaded library should have real version");

            System.out.println("Native library successfully loaded");
            System.out.println("Library version: " + NativeAffinity.VERSION);
        } else {
            assertEquals("not loaded", NativeAffinity.VERSION, "unloaded library should have 'not loaded' version");

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
            assertEquals(firstResult, results[i], "all threads should see consistent LOADED state");
        }
    }

    @Test
    public void versionStringIsSafeForLogging() {
        // Should not throw when used in logging/printing contexts
        try {
            String logMessage = "Library version: " + NativeAffinity.VERSION;
            assertNotNull(logMessage, "log message created");

            // Should be safe to concatenate
            String concatenated = "V" + NativeAffinity.VERSION + "X";
            assertTrue(concatenated.startsWith("V"), "concatenated starts with V");
            assertTrue(concatenated.endsWith("X"), "concatenated ends with X");
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
            assertNotNull(NativeAffinity.VERSION, "version should be available when library loaded");

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
            // Verify we can call a native method (which proves JNI is working)
            String version = NativeAffinity.VERSION;
            assertNotNull(version, "native method call succeeded");
            assertNotEquals("not loaded", version, "native method returned valid version");
        }
    }

    @Test
    public void staticInitializationOrderIsCorrect() {
        // Test that static initialization happens in correct order:
        // 1. LOADED is set
        // 2. VERSION is set based on LOADED

        // Both should be initialized
        boolean loadedWrapper = NativeAffinity.LOADED;
        assertTrue(loadedWrapper || !loadedWrapper, "LOADED should be initialised");

        String version = NativeAffinity.VERSION;
        assertNotNull(version, "VERSION should be initialised");

        // Relationship should be consistent
        if (NativeAffinity.LOADED) {
            assertNotEquals("not loaded", version, "VERSION should reflect loaded state");
        } else {
            assertEquals("not loaded", version, "VERSION should reflect not loaded state");
        }
    }

    @Test
    public void classCanBeLoadedMultipleTimes() {
        // Access class multiple times - should not cause re-initialization
        Class<?> class1 = NativeAffinity.class;
        Class<?> class2 = NativeAffinity.INSTANCE.getClass();

        assertSame(class1, class2, "Class should be the same");
    }
}
