/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
