package net.openhft.affinity.impl;

import net.openhft.affinity.AffinitySupport;
import net.openhft.affinity.IAffinity;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import static org.junit.Assert.assertTrue;

/**
 * Created by andre on 22/06/15.
 */
public class NativeAffinityImpTest extends AbstractAffinityImplTest
{
    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
        Assume.assumeTrue("linux".equalsIgnoreCase(System.getProperty("os.name")));
    }

    @Override
    public IAffinity getImpl()
    {
        return NativeAffinity.INSTANCE;
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
