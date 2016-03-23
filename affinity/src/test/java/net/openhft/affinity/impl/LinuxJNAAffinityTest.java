/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.openhft.affinity.impl;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter on 23/03/16.
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
