/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.AbstractAffinityImplTest;
import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author peter.lawrey
 */
public class JnaAffinityTest extends AbstractAffinityImplTest {
    @BeforeAll
    public static void checkJniLibraryPresent() {
        assumeTrue(LinuxJNAAffinity.LOADED, "requires LinuxJNAAffinity to be loaded");
    }

    @Test
    public void showOtherIds() {
        long processId = LinuxJNAAffinity.INSTANCE.getProcessId();
        long threadId = LinuxJNAAffinity.INSTANCE.getThreadId();
        int cpu = LinuxJNAAffinity.INSTANCE.getCpu();
        System.out.println("processId: " + processId);
        System.out.println("threadId: " + threadId);
        System.out.println("cpu: " + cpu);
        assertTrue(processId > 0, "processId should be positive");
        assertTrue(threadId > 0, "threadId should be positive");
        assertTrue(cpu >= 0, "cpu should be non-negative");
    }

    @Override
    protected IAffinity getImpl() {
        return LinuxJNAAffinity.INSTANCE;
    }
}
