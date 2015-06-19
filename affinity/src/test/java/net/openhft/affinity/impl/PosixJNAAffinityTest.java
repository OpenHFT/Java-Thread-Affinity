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

import net.openhft.affinity.AffinitySupport;
import net.openhft.affinity.IAffinity;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author peter.lawrey
 */
public class PosixJNAAffinityTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")));
    }

    @Override
    public IAffinity getImpl() {
        return AffinitySupport.getAffinityImpl();
    }

    @Test
    public void testGettid() {
        System.out.println("pid=" + getImpl().getProcessId());
        System.out.println("tid=" + getImpl().getThreadId());
        AffinitySupport.setThreadId();

        for (int j = 0; j < 3; j++) {
            final int runs = 100000;
            long tid = 0;
            long time = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                tid = Thread.currentThread().getId();
                time += System.nanoTime() - start;
                assertTrue(tid > 0);
                assertTrue(tid < 1 << 16);
            }
            System.out.printf("gettid took an average of %,d ns, tid=%d%n", time / runs, tid);
        }
    }
}
