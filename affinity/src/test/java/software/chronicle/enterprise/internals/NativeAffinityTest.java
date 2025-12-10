/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.AbstractAffinityImplTest;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
    }

    @Test
    public void showOtherIds() {
        System.out.println("processId: " + NativeAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + NativeAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + NativeAffinity.INSTANCE.getCpu());
    }

    @Override
    protected IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }
}
