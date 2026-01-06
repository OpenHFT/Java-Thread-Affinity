/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity.impl;

import net.openhft.affinity.BaseAffinitySupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/*
 * Created by Peter Lawrey on 23/03/16.
 */
public class LinuxJNAAffinityTest extends BaseAffinitySupport {
    @BeforeAll
    public static void checkJniLibraryPresent() {
        assumeTrue(LinuxJNAAffinity.LOADED, "requires LinuxJNAAffinity to be loaded");
    }

    @Test
    public void linuxJna() {
        int nbits = Runtime.getRuntime().availableProcessors();
        BitSet affinity0 = LinuxJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity0);

        BitSet affinity = new BitSet(nbits);

        affinity.set(1);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity2 = LinuxJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity2);
        assertEquals(1, LinuxJNAAffinity.INSTANCE.getCpu(), "cpu id after setting affinity");
        assertEquals(affinity, affinity2, "affinity mask round-trip");

        affinity.set(0, nbits);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
    }
}
