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

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/*
 * Created by Peter Lawrey on 23/03/16.
 */
public class LinuxJNAAffinityTest {
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(LinuxJNAAffinity.LOADED);
    }

    @Test
    public void LinuxJNA() {
        int nbits = Runtime.getRuntime().availableProcessors();
        BitSet affinity0 = LinuxJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity0);

        BitSet affinity = new BitSet(nbits);

        affinity.set(1);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity2 = LinuxJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity2);
        assertEquals(1, LinuxJNAAffinity.INSTANCE.getCpu());
        assertEquals(affinity, affinity2);

        affinity.set(0, nbits);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
    }
}
