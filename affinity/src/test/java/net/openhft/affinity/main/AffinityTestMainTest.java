/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.main;

import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.impl.NoCpuLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class AffinityTestMainTest {

    private long originalSleepMillis;

    @Before
    public void reduceWorkerSleepInterval() {
        originalSleepMillis = AffinityTestMain.getWorkSleepMillisForTests();
        AffinityTestMain.setWorkSleepMillisForTests(5L);
    }

    @After
    public void restoreWorkerSleepInterval() {
        AffinityTestMain.setWorkSleepMillisForTests(originalSleepMillis);
    }

    @Test
    public void workerThreadStopsWhenInterrupted() throws Exception {
        LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        Supplier<AffinityLock> lockSupplier = AffinityTestMainTest::createTestLock;

        Thread worker = AffinityTestMain.createWorkerThread(lockSupplier, messages::add);
        worker.start();

        String lockMessage = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull("Worker did not report lock acquisition", lockMessage);
        assertTrue(lockMessage.contains("locked onto cpu"));

        // Await one work-loop message before interrupting to ensure the loop started.
        String workMessage = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull("Worker did not emit work message", workMessage);
        assertTrue(workMessage.contains("doing work on cpu"));

        worker.interrupt();

        String interruptionMessage = messages.poll(1, TimeUnit.SECONDS);
        assertEquals("Thread interrupted; exiting work loop", interruptionMessage);

        worker.join(2_000L);
        assertFalse("Worker thread should exit after interruption", worker.isAlive());
    }

    private static AffinityLock createTestLock() {
        try {
            Class<?> inventoryClass = Class.forName("net.openhft.affinity.LockInventory");

            Constructor<?> inventoryConstructor = inventoryClass.getDeclaredConstructor(net.openhft.affinity.CpuLayout.class);
            inventoryConstructor.setAccessible(true);
            Object inventory = inventoryConstructor.newInstance(new NoCpuLayout(1));

            Constructor<AffinityLock> lockConstructor = AffinityLock.class.getDeclaredConstructor(int.class, int.class, boolean.class, boolean.class, inventoryClass);
            lockConstructor.setAccessible(true);
            return lockConstructor.newInstance(0, 0, true, false, inventory);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to construct test affinity lock", e);
        }
    }
}
