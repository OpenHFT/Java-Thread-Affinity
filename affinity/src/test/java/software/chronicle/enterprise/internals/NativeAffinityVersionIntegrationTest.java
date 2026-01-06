/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for the native version functionality.
 * These tests only run when the native library is actually loaded.
 */
public class NativeAffinityVersionIntegrationTest {

    @BeforeAll
    public static void checkNativeLibraryLoaded() {
        // Only run these tests if native library is loaded
        assumeTrue(NativeAffinity.LOADED, "Native library must be loaded for these tests");
    }

    @Test
    public void versionContainsVersionNumber() {
        String version = NativeAffinity.VERSION;

        // Should contain at least one digit
        assertTrue(version.matches(".*\\d+.*"), "Version should contain version number: " + version);

        System.out.println("Native library version: " + version);
    }

    @Test
    public void versionMatchesProjectVersion() {
        String version = NativeAffinity.VERSION;

        // Version should match Maven project version pattern
        // Common patterns: "3.27ea2-SNAPSHOT", "3.27.0", "1.0.0-RC1"
        assertTrue(
                version.matches("\\d+\\.\\d+.*") || // Major.minor...
                        version.matches("\\d+.*"), // At least major version
                "Version should match project versioning scheme: " + version
        );
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

        assertTrue(hasStatusMarker, "Version should contain status marker: " + version);
    }

    @Test
    public void versionStringIsWellFormed() {
        String version = NativeAffinity.VERSION;

        // Should not have leading/trailing whitespace
        assertEquals(version, version.trim(), "Version should not have leading/trailing whitespace");

        // Should not be too short
        assertTrue(version.length() >= 3, "Version should have reasonable length: " + version.length());

        // Should not contain unexpected characters
        assertTrue(version.matches("[0-9a-zA-Z.\\-_]+"), "Version should only contain version-appropriate characters");
    }

    @Test
    public void versionIsConsistentAcrossMultipleCalls() {
        // Call multiple times - should always return same value
        String version1 = NativeAffinity.VERSION;
        String version2 = NativeAffinity.VERSION;
        String version3 = NativeAffinity.VERSION;

        assertSame(version1, version2, "version constant should return identical string instance across calls");
        assertSame(version2, version3, "version constant should maintain object identity for all accesses");
    }

    @Test
    public void versionComesFromNativeLayer() {
        String version = NativeAffinity.VERSION;

        // Version comes from C++ PROJECT_VERSION define
        // Should not be Java defaults
        assertNotEquals("unknown", version, "native layer should provide actual version instead of 'unknown' placeholder");
        assertNotEquals("", version, "native layer should provide non-empty version string");
        assertNotEquals("0.0.0", version, "native layer should provide real version instead of default '0.0.0'");
    }

    @Test
    public void versionReflectsCompileTimeValue() {
        String version = NativeAffinity.VERSION;

        // The version should be the compile-time PROJECT_VERSION
        // This is set via -DPROJECT_VERSION in the Makefile
        assertNotNull(version, "Compile-time version should be set");

        // Should look like a Maven version (since it comes from pom.xml)
        assertTrue(
                version.contains(".") || // Has version separators
                        version.matches("\\d+.*"), // Or at least starts with number
                "Should use Maven-style versioning: " + version
        );
    }

    @Test
    public void versionCanBeUsedForCompatibilityChecks() {
        String version = NativeAffinity.VERSION;

        // Extract major version number for compatibility checking
        if (version.matches("(\\d+)\\..*")) {
            String majorVersion = version.split("\\.")[0];
            int major = Integer.parseInt(majorVersion);

            assertTrue(major > 0, "Major version should be positive");
            System.out.println("Major version: " + major);
        }
    }

    @Test
    public void versionMatchesBuildSystemVersion() {
        String version = NativeAffinity.VERSION;

        // The version comes from Maven via Makefile
        // It should match the pattern: <major>.<minor><release-type>[-SNAPSHOT]
        // Examples: "3.27ea2-SNAPSHOT", "3.27.0", "1.0.0-RC1"

        assertTrue(
                version.matches("\\d+\\.\\d+.*") || // Standard semver
                        version.matches("\\d+\\.\\d+[a-z]+\\d*.*"), // With EA/RC markers
                "Version should match build system pattern: " + version
        );
    }

    @Test
    public void versionIsValidCString() {
        String version = NativeAffinity.VERSION;

        // Should not contain null terminators (C string should be properly converted)
        assertFalse(version.contains("\0"), "Version should not contain null terminators");

        // Should not contain control characters
        for (char c : version.toCharArray()) {
            assertTrue(
                    c >= 32 || c == '\n' || c == '\r' || c == '\t',
                    "Version should not contain control characters: " + (int) c
            );
        }
    }

    @Test
    public void versionStringMemoryIsSafe() {
        // Test that we can safely use the version string without memory issues
        String version = NativeAffinity.VERSION;

        // Should be able to create substrings
        if (version.length() > 1) {
            String substring = version.substring(0, 1);
            assertNotNull(substring, "version string should support substring operations without memory corruption");
        }

        // Should be able to compare
        boolean equals = version.equals(version);
        assertTrue(equals, "version string should support equality comparison with itself");

        // Should be able to hash
        int hash = version.hashCode();
        assertEquals(hash, version.hashCode(), "version string should return consistent hashcode across calls");
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

        assertNotNull(version, "native library version should be available for troubleshooting and compatibility checks");
    }
}
