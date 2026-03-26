/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.Affinity;
import net.openhft.affinity.IAffinity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/*
 * Created by andre on 22/06/15.
 */
class NativeAffinityImpTest extends AbstractAffinityImplTest {
    @BeforeAll
    static void checkJniLibraryPresent() {
        assumeTrue(NativeAffinity.LOADED);
        assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")));
    }

    @Override
    public IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }

    @Test
    void testGettid() {
        System.out.println("pid=" + getImpl().getProcessId());
        System.out.println("tid=" + getImpl().getThreadId());
        Affinity.setThreadId();

        for (int j = 0; j < 3; j++) {
            final int runs = 100000;
            long tid = 0;
            long time = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                @SuppressWarnings("deprecation")
                long tid0 = Thread.currentThread().getId();
                tid = tid0;
                time += System.nanoTime() - start;
                assertTrue(tid > 0);
                assertTrue(tid < 1 << 16);
            }
            System.out.printf("gettid took an average of %,d ns, tid=%d%n", time / runs, tid);
        }
    }
}
