/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class AffinityThreadFactoryTest extends BaseAffinitySupport {

    @BeforeEach
    public void checkLinux() {
        assumeTrue(LockCheck.IS_LINUX, "requires Linux");
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
            es.execute(() -> {
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

        assertTrue(finished.await(5, TimeUnit.SECONDS), "threads finished within timeout");
        es.shutdown();
        es.awaitTermination(5, TimeUnit.SECONDS);

        assertFalse(cpus.contains(-1), "cpu id should never be -1");
        assertEquals(nThreads, cpus.size(), "each thread records a distinct CPU id");
    }
}
