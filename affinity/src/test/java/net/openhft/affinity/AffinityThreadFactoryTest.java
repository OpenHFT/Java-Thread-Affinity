package net.openhft.affinity;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AffinityThreadFactoryTest extends BaseAffinityTest {

    @Before
    public void checkLinux() {
        Assume.assumeTrue(LockCheck.IS_LINUX);
    }

    @Test
    public void threadsReceiveDistinctCpus() throws InterruptedException {
        int available = Math.max(1, AffinityLock.PROCESSORS - 1);
        int nThreads = Math.min(4, available);

        ExecutorService es = Executors.newFixedThreadPool(nThreads,
                new AffinityThreadFactory("test"));

        Set<Integer> cpus = ConcurrentHashMap.newKeySet();
        CountDownLatch ready = new CountDownLatch(nThreads);
        CountDownLatch finished = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            es.submit(() -> {
                cpus.add(Affinity.getCpu());
                ready.countDown();
                try {
                    ready.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finished.countDown();
            });
        }

        assertTrue(finished.await(5, TimeUnit.SECONDS));
        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);

        assertFalse(cpus.contains(-1));
        assertEquals(nThreads, cpus.size());
    }
}
