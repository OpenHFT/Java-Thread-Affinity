//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.affinity;

import net.openhft.affinity.impl.VanillaCpuLayout;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class AffinityLockDumpLocksTest extends BaseAffinityTest {

    private static void supressUnusedWarning(AutoCloseable c) {
        // do nothing
    }

    @Test
    public void dumpLocksListsThreadsHoldingLocks() throws Exception {
        Assume.assumeTrue(new File("/proc/cpuinfo").exists());

        AffinityLock.cpuLayout(VanillaCpuLayout.fromCpuInfo());
        int nThreads = Math.min(3, Math.max(1, AffinityLock.PROCESSORS - 1));
        CountDownLatch acquired = new CountDownLatch(nThreads);
        CountDownLatch release = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            String name = "worker-" + i;
            Thread t = new Thread(() -> {
                try (AffinityLock lock = AffinityLock.acquireLock()) {
                    supressUnusedWarning(lock);
                    acquired.countDown();
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, name);
            threads.add(t);
            t.start();
        }

        assertTrue("threads failed to acquire locks", acquired.await(5, TimeUnit.SECONDS));

        String dump = AffinityLock.dumpLocks();
        for (Thread t : threads) {
            assertTrue("Missing entry for " + t.getName(), dump.contains(t.getName()));
        }

        release.countDown();
        for (Thread t : threads) {
            t.join();
        }
    }
}
