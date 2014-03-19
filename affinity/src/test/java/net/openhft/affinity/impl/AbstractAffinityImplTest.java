/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author cheremin
 * @since 29.12.11,  20:25
 */
public abstract class AbstractAffinityImplTest {

    protected static final int CORES = Runtime.getRuntime().availableProcessors();

    public abstract IAffinity getImpl();


    @Test
    public void getAffinityCompletesGracefully() throws Exception {
        getImpl().getAffinity();
    }

    @Test
    public void getAffinityReturnsValidValue() throws Exception {
        final long affinity = getImpl().getAffinity();
        assertTrue(
                "Affinity mask " + affinity + " must be >0",
                affinity > 0
        );
        final long allCoresMask = (1L << CORES) - 1;
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
            final long mask = (1L << core);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final long mask) throws Exception {

        impl.setAffinity(mask);
        final long _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() throws Exception {
        final long anyCore = (1L << CORES) - 1;
        try {
            getImpl().setAffinity(anyCore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
