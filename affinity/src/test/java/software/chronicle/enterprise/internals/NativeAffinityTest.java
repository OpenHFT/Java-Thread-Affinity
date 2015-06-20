/*
 * Copyright 2011 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest {
    protected static final int CORES = Runtime.getRuntime().availableProcessors();

    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
    }

    @Test
    public void getAffinityCompletesGracefully() throws Exception {
        System.out.println("affinity: " + Long.toBinaryString(getImpl().getAffinity()));
    }

    @Test
    public void getAffinityReturnsValidValue() throws Exception {
        final long affinity = getImpl().getAffinity();
        assertTrue(
                "Affinity mask " + affinity + " must be >0",
                affinity > 0
        );
        final int allCoresMask = (1 << CORES) - 1;
        assertTrue(
                "Affinity mask " + affinity + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")",
                affinity <= allCoresMask
        );
    }

    @Test
    public void setAffinityCompletesGracefully() throws Exception {
        getImpl().setAffinity(1);
    }

    @Test
    public void getAffinityReturnsValuePreviouslySet() throws Exception {
        final IAffinity impl = getImpl();
        final int cores = CORES;
        for (int core = 0; core < cores; core++) {
            final long mask = (1 << core);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    @Test
    public void showOtherIds() {
        System.out.println("processId: " + NativeAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + NativeAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + NativeAffinity.INSTANCE.getCpu());
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final long mask) throws Exception {

        impl.setAffinity(mask);
        final long _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() throws Exception {
        final int anyCore = (1 << CORES) - 1;
        getImpl().setAffinity(anyCore);
    }

    public IAffinity getImpl() {
        return NativeAffinity.INSTANCE;
    }
}
