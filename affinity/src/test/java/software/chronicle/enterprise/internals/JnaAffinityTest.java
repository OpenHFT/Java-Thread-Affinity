/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.AbstractAffinityImplTest;
import net.openhft.affinity.impl.LinuxJNAAffinity;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author peter.lawrey
 */
public class JnaAffinityTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(LinuxJNAAffinity.LOADED);
    }

    @Test
    public void showOtherIds() {
        System.out.println("processId: " + LinuxJNAAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + LinuxJNAAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + LinuxJNAAffinity.INSTANCE.getCpu());
    }

    @Override
    protected IAffinity getImpl() {
        return LinuxJNAAffinity.INSTANCE;
    }
}
