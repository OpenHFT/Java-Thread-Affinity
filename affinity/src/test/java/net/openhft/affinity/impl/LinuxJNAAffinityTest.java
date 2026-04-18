/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinityTest;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/*
 * Created by Peter Lawrey on 23/03/16.
 */
public class LinuxJNAAffinityTest extends BaseAffinityTest {
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
