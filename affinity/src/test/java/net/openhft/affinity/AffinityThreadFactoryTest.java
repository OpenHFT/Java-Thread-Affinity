package net.openhft.affinity;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AffinityThreadFactoryTest {
    private AffinityThreadFactory factory;

    @Before
    public void setUp() {
        factory = new AffinityThreadFactory("TestThread");
    }

    @After
    public void tearDown() {
        factory = null;
    }

    @Test
    public void testThreadFactoryWithDefaultStrategies() {
        AffinityThreadFactory defaultFactory = new AffinityThreadFactory("DefaultThread");
        assertNotNull(defaultFactory);
    }

    @Test
    public void testThreadFactoryWithCustomStrategies() {
        AffinityThreadFactory customFactory = new AffinityThreadFactory("CustomThread", AffinityStrategies.DIFFERENT_SOCKET, AffinityStrategies.SAME_CORE);
        assertNotNull(customFactory);
    }

    @Test
    public void testThreadFactoryWithDaemonFlag() {
        AffinityThreadFactory daemonFactory = new AffinityThreadFactory("DaemonThread", true, AffinityStrategies.ANY);
        assertNotNull(daemonFactory);
    }

    @Test
    public void testNewThread() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Test Runnable
            }
        };
        Thread thread = factory.newThread(runnable);
        assertNotNull(thread);
        assertEquals("TestThread", thread.getName());
        assertTrue(thread.isDaemon());
    }

    @Test
    public void testNewThreadWithIncrementingId() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Test Runnable
            }
        };
        Thread thread1 = factory.newThread(runnable);
        Thread thread2 = factory.newThread(runnable);
        assertNotNull(thread1);
        assertNotNull(thread2);
        assertEquals("TestThread", thread1.getName());
        assertEquals("TestThread-2", thread2.getName());
    }

    @Test
    public void testAcquireLockBasedOnLast() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Test Runnable
            }
        };
        Thread thread = factory.newThread(runnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertNotNull(factory);
    }

    @Test
    public void testAffinityLockAcquire() {
        AffinityLock lock = factory.acquireLockBasedOnLast();
        assertNotNull(lock);
    }
}
