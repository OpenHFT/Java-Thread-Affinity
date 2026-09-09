/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.io.File;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@EnabledOnOs(OS.LINUX)
// Match the make-c profile's ARM32 exclusion; aarch64 remains eligible.
@DisabledIfSystemProperty(named = "os.arch", matches = "(?i)arm")
public class NativeAffinityIntegrationTest extends AffinityTestProcess {
    @Test
    void roundTripsShortMask() throws Exception {
        runNativeProbe("short-mask");
    }

    @Test
    void agreesWithJna() throws Exception {
        runNativeProbe("jna-parity");
    }

    @Test
    void failedSetReportsErrnoWithoutChangingMask() throws Exception {
        runNativeProbe("failed-set");
    }

    @Test
    void repeatedReadsPassJniChecks() throws Exception {
        runNativeProbe("repeated-reads");
    }

    private void runNativeProbe(String scenario) throws Exception {
        assumeFalse(System.getProperties().containsKey("dontMake"), "Native build explicitly disabled with dontMake");
        File classes = new File(NativeAffinity.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        assertTrue(new File(classes, System.mapLibraryName("CEInternals")).isFile(), "Build the JNI library before testing");
        String libraryPath = "-Djava.library.path=" + classes.getAbsolutePath();
        if (scenario.equals("jna-parity")) {
            // Older third-party jnidispatch builds emit their own -Xcheck:jni warnings.
            runProbe(NativeAffinityIntegrationTest.class, scenario, true, libraryPath);
        } else {
            // Check only this checkout's JNI library, with JNA absent from the child classpath.
            runProbe(NativeAffinityIntegrationTest.class, scenario, false, "-Xcheck:jni", libraryPath);
        }
    }

    public static void main(String[] args) {
        String scenario = System.getProperty("affinity.test.scenario");
        assertTrue(NativeAffinity.LOADED, "The library built by this checkout must load");
        NativeAffinity nativeAffinity = NativeAffinity.INSTANCE;
        BitSet original = nativeAffinity.getAffinity();
        assertNotNull(original);
        assertFalse(original.isEmpty());
        try {
            if (scenario.equals("short-mask") || scenario.equals("jna-parity")) {
                BitSet single = new BitSet();
                single.set(original.nextSetBit(0));
                if (scenario.equals("short-mask") && single.toByteArray().length >= 128) {
                    System.out.println("SKIP short-mask: no permitted CPU fits in fewer than 128 bytes");
                    return;
                }
                nativeAffinity.setAffinity(single);
                assertEquals(single, nativeAffinity.getAffinity());
                if (scenario.equals("jna-parity")) {
                    assertTrue(LinuxJNAAffinity.LOADED);
                    assertEquals(single, LinuxJNAAffinity.INSTANCE.getAffinity());
                    LinuxJNAAffinity.INSTANCE.setAffinity(original);
                    assertEquals(original, nativeAffinity.getAffinity());
                }
            } else if (scenario.equals("failed-set")) {
                RuntimeException failure = assertThrows(RuntimeException.class,
                        () -> nativeAffinity.setAffinity(new BitSet()));
                assertTrue(failure.getMessage().contains("sched_setaffinity"), failure.getMessage());
                assertTrue(failure.getMessage().contains("maskBytes=0"), failure.getMessage());
                assertTrue(failure.getMessage().contains("errno=22"), failure.getMessage());
                assertEquals(original, nativeAffinity.getAffinity());
            } else if (scenario.equals("repeated-reads")) {
                for (int i = 0; i < 10_000; i++) {
                    assertEquals(original, nativeAffinity.getAffinity());
                }
            } else {
                fail("Unknown probe: " + scenario);
            }
        } finally {
            nativeAffinity.setAffinity(original);
            assertEquals(original, nativeAffinity.getAffinity(), "Restore the exact original permitted mask");
        }
        System.out.println("PASS " + scenario + " original=" + original);
    }
}
