/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.IAffinity;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assume.assumeTrue;

/**
 * @author peter.lawrey
 */
public class PosixJNAAffinityTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        assumeTrue("TODO FIX JNA library is not used, but the test is flaky", false);
        assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")));
    }

    @Override
    public IAffinity getImpl() {
        return Affinity.getAffinityImpl();
    }

    @Test
    public void testGettid() {
        runThreadIdBenchmark(1 << 24);
    }
}
