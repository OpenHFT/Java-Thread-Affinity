package net.openhft.affinity.impl;

import net.openhft.affinity.IAffinity;
import org.junit.Assume;
import org.junit.BeforeClass;

public class OSXJNAAffinityTest extends AbstractAffinityImplTest {
    @BeforeClass
    public static void checkJnaLibraryPresent() {
        Assume.assumeTrue(System.getProperty("os.name").startsWith("Mac"));
    }

    @Override
    public IAffinity getImpl() {
        return OSXJNAAffinity.INSTANCE;
    }
}
