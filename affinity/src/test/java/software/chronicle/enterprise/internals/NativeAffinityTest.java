/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.AbstractAffinityImplTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest extends AbstractAffinityImplTest {
    @BeforeAll
    public static void checkJniLibraryPresent() {
        assumeTrue(NativeAffinity.LOADED, "requires NativeAffinity to be loaded");
    }

    @Test
    public void showOtherIds() {
        long processId = NativeAffinity.INSTANCE.getProcessId();
        long threadId = NativeAffinity.INSTANCE.getThreadId();
        int cpu = NativeAffinity.INSTANCE.getCpu();
        System.out.println("processId: " + processId);
        System.out.println("threadId: " + threadId);
        System.out.println("cpu: " + cpu);
        assertTrue(processId > 0, "processId should be positive");
        assertTrue(threadId > 0, "threadId should be positive");
        assertTrue(cpu >= 0, "cpu should be non-negative");
    }

    @Override
    protected IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }
}
