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

import net.openhft.affinity.Affinity;
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
        return Affinity.getAffinityImpl();
    }

    @Test
    public void testGettid() {
        System.out.println("pid=" + getImpl().getProcessId());
        System.out.println("tid=" + getImpl().getThreadId());
        Affinity.setThreadId();

        for (int j = 0; j < 3; j++) {
            final int runs = 100000;
            long tid = 0;
            long time = 0;
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                tid = Thread.currentThread().getId();
                time += System.nanoTime() - start;
                assertTrue(tid > 0);
                assertTrue(tid < 1 << 24);
            }
            System.out.printf("gettid took an average of %,d ns, tid=%d%n", time / runs, tid);
        }
    }
}
