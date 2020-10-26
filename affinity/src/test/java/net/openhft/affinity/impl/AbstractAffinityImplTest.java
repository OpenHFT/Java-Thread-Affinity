/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.junit.After;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author cheremin
 * @since 29.12.11,  20:25
 */
public abstract class AbstractAffinityImplTest {

    protected static final int CORES = Runtime.getRuntime().availableProcessors();
    protected static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    public abstract IAffinity getImpl();

    @Test
    public void getAffinityCompletesGracefully() {
        getImpl().getAffinity();
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertTrue(
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty",
                !affinity.isEmpty()
        );
        final long allCoresMask = (1L << CORES) - 1;
        assertTrue(
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")",
                affinity.length() <= CORES_MASK.length()
        );
    }

    @Test
    public void setAffinityCompletesGracefully() {
        BitSet affinity = new BitSet(1);
        affinity.set(0, true);
        getImpl().setAffinity(affinity);
    }

    @Test
    public void getAffinityReturnsValuePreviouslySet() {
        final IAffinity impl = getImpl();
        for (int core = 0; core < CORES; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() {
        try {
            getImpl().setAffinity(CORES_MASK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
