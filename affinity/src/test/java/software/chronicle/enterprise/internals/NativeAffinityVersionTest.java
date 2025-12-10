/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.Assert.*;

/**
 * Tests for the native library version tracking functionality.
 * Exercises the new version reporting features added to NativeAffinity.
 */
public class NativeAffinityVersionTest {

    @Test
    public void versionConstantIsNotNull() {
        assertNotNull("NativeAffinity.VERSION should never be null", NativeAffinity.VERSION);
    }

    @Test
    public void versionConstantIsNotEmpty() {
        assertFalse("NativeAffinity.VERSION should not be empty", NativeAffinity.VERSION.isEmpty());
    }

    @Test
    public void versionHasExpectedValueWhenLoaded() {
        if (NativeAffinity.LOADED) {
            // When library is loaded, version should not be "not loaded"
            assertNotEquals("When library is loaded, VERSION should contain actual version",
                    "not loaded", NativeAffinity.VERSION);

            // Should contain something that looks like a version
            // (numbers, dots, hyphens, or letters for SNAPSHOT/ea etc)
            assertTrue("VERSION should match version pattern when loaded: " + NativeAffinity.VERSION,
                    NativeAffinity.VERSION.matches(".*\\d+.*"));

            System.out.println("Native library version: " + NativeAffinity.VERSION);
        }
    }

    @Test
    public void versionIsNotLoadedWhenLibraryNotLoaded() {
        if (!NativeAffinity.LOADED) {
            assertEquals("When library is not loaded, VERSION should be 'not loaded'",
                    "not loaded", NativeAffinity.VERSION);
        }
    }

    @Test
    public void versionFormatIsValid() {
        // Version should be either "not loaded" or a valid version string
        assertTrue("VERSION should be either 'not loaded' or contain version info",
                NativeAffinity.VERSION.equals("not loaded") ||
                NativeAffinity.VERSION.matches(".*[0-9]+.*"));
    }

    @Test
    public void versionDoesNotContainNullCharacters() {
        assertFalse("VERSION should not contain null characters",
                NativeAffinity.VERSION.contains("\0"));
    }

    @Test
    public void versionLengthIsReasonable() {
        assertTrue("VERSION length should be reasonable (< 100 chars): " + NativeAffinity.VERSION.length(),
                NativeAffinity.VERSION.length() < 100);
        assertTrue("VERSION length should be at least 1: " + NativeAffinity.VERSION.length(),
                NativeAffinity.VERSION.length() >= 1);
    }

    @Test
    public void versionIsPrintable() {
        // All characters should be printable (ASCII 32-126) or standard version chars
        for (char c : NativeAffinity.VERSION.toCharArray()) {
            assertTrue("VERSION should only contain printable characters, found: " + (int)c,
                    (c >= 32 && c <= 126) || Character.isWhitespace(c));
        }
    }

    @Test
    public void loadedStateIsConsistentWithVersion() {
        // If LOADED is true, VERSION should not be "not loaded"
        // If LOADED is false, VERSION should be "not loaded"
        if (NativeAffinity.LOADED) {
            assertNotEquals("When LOADED is true, VERSION should not be 'not loaded'",
                    "not loaded", NativeAffinity.VERSION);
        } else {
            assertEquals("When LOADED is false, VERSION should be 'not loaded'",
                    "not loaded", NativeAffinity.VERSION);
        }
    }

    @Test
    public void versionMatchesExpectedPattern() {
        if (NativeAffinity.LOADED) {
            // Expected patterns: "3.27ea2-SNAPSHOT", "3.27.0", "1.2.3-SNAPSHOT", etc.
            assertTrue("VERSION should match semantic versioning pattern: " + NativeAffinity.VERSION,
                    NativeAffinity.VERSION.matches("\\d+\\.\\d+.*") || // Basic semver
                    NativeAffinity.VERSION.matches(".*\\d+.*-.*") || // Version with suffix
                    NativeAffinity.VERSION.matches("\\d+.*")); // Any version starting with digit
        }
    }

    @Test
    public void versionCanBePrintedSafely() {
        // Should not throw when converting to string or printing
        String versionStr = NativeAffinity.VERSION;
        assertNotNull(versionStr);

        // Should be safe to print
        System.out.println("NativeAffinity version: " + NativeAffinity.VERSION);
    }

    @Test
    public void versionIsAccessibleFromInstance() {
        // Verify static VERSION is accessible and consistent
        String version1 = NativeAffinity.VERSION;
        String version2 = NativeAffinity.VERSION;

        assertSame("VERSION should be the same instance", version1, version2);
    }

    @Test
    public void versionDoesNotChangeAfterInitialization() {
        // VERSION should be stable after class loading
        String initialVersion = NativeAffinity.VERSION;

        // Force some operations
        @SuppressWarnings("unused")
        NativeAffinity instance = NativeAffinity.INSTANCE;

        // Version should still be the same
        assertSame("VERSION should not change after initialization",
                initialVersion, NativeAffinity.VERSION);
    }
}
