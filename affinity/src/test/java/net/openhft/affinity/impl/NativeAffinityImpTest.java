/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/*
 * Created by andre on 22/06/15.
 */
public class NativeAffinityImpTest extends AbstractAffinityImplTest {
    @BeforeAll
    public static void checkJniLibraryPresent() {
        assumeTrue(NativeAffinity.LOADED, "requires NativeAffinity to be loaded");
        assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")), "requires Linux");
    }

    @Override
    public IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }

    @Test
    public void testGettid() {
        assertDoesNotThrow(() -> runThreadIdBenchmark(1 << 16), "gettid benchmark completes");
    }
}
