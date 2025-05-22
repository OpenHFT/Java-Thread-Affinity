package net.openhft.affinity.impl;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class WindowsJNAAffinityTest {
    @BeforeClass
    public static void checkJnaLibraryPresent() {
        Assume.assumeTrue(WindowsJNAAffinity.LOADED);
    }

    @Test
    public void windowsJNA() {
        int nbits = Runtime.getRuntime().availableProcessors();
        BitSet affinity0 = WindowsJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity0);

        BitSet affinity = new BitSet(nbits);
        affinity.set(0);
        WindowsJNAAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity2 = WindowsJNAAffinity.INSTANCE.getAffinity();
        System.out.println(affinity2);
        assertEquals(affinity, affinity2);

        affinity.set(0, nbits);
        WindowsJNAAffinity.INSTANCE.setAffinity(affinity);
    }
}
