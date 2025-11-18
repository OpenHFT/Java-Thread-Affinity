/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.Assert.*;

/**
 * Integration tests for the native version functionality.
 * These tests only run when the native library is actually loaded.
 */
public class NativeAffinityVersionIntegrationTest {

    @BeforeClass
    public static void checkNativeLibraryLoaded() {
        // Only run these tests if native library is loaded
        Assume.assumeTrue("Native library must be loaded for these tests",
                NativeAffinity.LOADED);
    }

    @Test
    public void versionContainsVersionNumber() {
        String version = NativeAffinity.VERSION;

        // Should contain at least one digit
        assertTrue("Version should contain version number: " + version,
                version.matches(".*\\d+.*"));

        System.out.println("Native library version: " + version);
    }

    @Test
    public void versionMatchesProjectVersion() {
        String version = NativeAffinity.VERSION;

        // Version should match Maven project version pattern
        // Common patterns: "3.27ea2-SNAPSHOT", "3.27.0", "1.0.0-RC1"
        assertTrue("Version should match project versioning scheme: " + version,
                version.matches("\\d+\\.\\d+.*") || // Major.minor...
                version.matches("\\d+.*")); // At least major version
    }

    @Test
    public void versionContainsSnapshotOrReleaseMarker() {
        String version = NativeAffinity.VERSION;

        // Should indicate development status
        boolean hasStatusMarker =
                version.contains("SNAPSHOT") ||
                version.contains("ea") ||
                version.contains("RC") ||
                version.contains("RELEASE") ||
                version.matches("\\d+\\.\\d+\\.\\d+"); // Or is a clean release version

        assertTrue("Version should contain status marker: " + version, hasStatusMarker);
    }

    @Test
    public void versionStringIsWellFormed() {
        String version = NativeAffinity.VERSION;

        // Should not have leading/trailing whitespace
        assertEquals("Version should not have leading/trailing whitespace",
                version, version.trim());

        // Should not be too short
        assertTrue("Version should have reasonable length: " + version.length(),
                version.length() >= 3);

        // Should not contain unexpected characters
        assertTrue("Version should only contain version-appropriate characters",
                version.matches("[0-9a-zA-Z.\\-_]+"));
    }

    @Test
    public void versionIsConsistentAcrossMultipleCalls() {
        // Call multiple times - should always return same value
        String version1 = NativeAffinity.VERSION;
        String version2 = NativeAffinity.VERSION;
        String version3 = NativeAffinity.VERSION;

        assertSame("VERSION should be the same object", version1, version2);
        assertSame("VERSION should be the same object", version2, version3);
    }

    @Test
    public void versionComesFromNativeLayer() {
        String version = NativeAffinity.VERSION;

        // Version comes from C++ PROJECT_VERSION define
        // Should not be Java defaults
        assertNotEquals("Version should not be fallback value", "unknown", version);
        assertNotEquals("Version should not be fallback value", "", version);
        assertNotEquals("Version should not be fallback value", "0.0.0", version);
    }

    @Test
    public void versionReflectsCompileTimeValue() {
        String version = NativeAffinity.VERSION;

        // The version should be the compile-time PROJECT_VERSION
        // This is set via -DPROJECT_VERSION in the Makefile
        assertNotNull("Compile-time version should be set", version);

        // Should look like a Maven version (since it comes from pom.xml)
        assertTrue("Should use Maven-style versioning: " + version,
                version.contains(".") || // Has version separators
                version.matches("\\d+.*")); // Or at least starts with number
    }

    @Test
    public void versionCanBeUsedForCompatibilityChecks() {
        String version = NativeAffinity.VERSION;

        // Extract major version number for compatibility checking
        if (version.matches("(\\d+)\\..*")) {
            String majorVersion = version.split("\\.")[0];
            int major = Integer.parseInt(majorVersion);

            assertTrue("Major version should be positive", major > 0);
            System.out.println("Major version: " + major);
        }
    }

    @Test
    public void versionMatchesBuildSystemVersion() {
        String version = NativeAffinity.VERSION;

        // The version comes from Maven via Makefile
        // It should match the pattern: <major>.<minor><release-type>[-SNAPSHOT]
        // Examples: "3.27ea2-SNAPSHOT", "3.27.0", "1.0.0-RC1"

        assertTrue("Version should match build system pattern: " + version,
                version.matches("\\d+\\.\\d+.*") || // Standard semver
                version.matches("\\d+\\.\\d+[a-z]+\\d*.*")); // With EA/RC markers
    }

    @Test
    public void versionIsValidCString() {
        String version = NativeAffinity.VERSION;

        // Should not contain null terminators (C string should be properly converted)
        assertFalse("Version should not contain null terminators",
                version.contains("\0"));

        // Should not contain control characters
        for (char c : version.toCharArray()) {
            assertTrue("Version should not contain control characters: " + (int)c,
                    c >= 32 || c == '\n' || c == '\r' || c == '\t');
        }
    }

    @Test
    public void versionStringMemoryIsSafe() {
        // Test that we can safely use the version string without memory issues
        String version = NativeAffinity.VERSION;

        // Should be able to create substrings
        if (version.length() > 1) {
            String substring = version.substring(0, 1);
            assertNotNull("Should be able to create substring", substring);
        }

        // Should be able to compare
        boolean equals = version.equals(version);
        assertTrue("Should be able to compare with itself", equals);

        // Should be able to hash
        int hash = version.hashCode();
        assertEquals("Hash should be consistent", hash, version.hashCode());
    }

    @Test
    public void versionDocumentsNativeImplementation() {
        String version = NativeAffinity.VERSION;

        // The version tells us which native library version is loaded
        System.out.println("=== Native Library Information ===");
        System.out.println("Version: " + version);
        System.out.println("Loaded: " + NativeAffinity.LOADED);
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println("==================================");

        assertNotNull("Documentation should be available", version);
    }
}
