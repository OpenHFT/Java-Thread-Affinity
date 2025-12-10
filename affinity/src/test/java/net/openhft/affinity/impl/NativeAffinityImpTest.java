/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

/*
 * Created by andre on 22/06/15.
 */
public class NativeAffinityImpTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
        Assume.assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")));
    }

    @Override
    public IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }

    @Test
    public void testGettid() {
        runThreadIdBenchmark(1 << 16);
    }
}
