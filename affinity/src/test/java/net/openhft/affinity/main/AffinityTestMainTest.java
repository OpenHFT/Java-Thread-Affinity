package net.openhft.affinity.main;

import junit.framework.TestCase;

public class AffinityTestMainTest extends TestCase {

    public void testMainWithNoArgs() {
        String[] args = {};
        Thread t = new Thread(() -> {
            try {
                AffinityTestMain.main(args);
            } catch (Exception e) {
                fail("Exception thrown: " + e.getMessage());
            }
        });
        t.start();
        try {
            Thread.sleep(1000); // Let it run for a short while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt();
    }

    public void testMainWithArgs() {
        String[] args = {"2"};
        Thread t = new Thread(() -> {
            try {
                AffinityTestMain.main(args);
            } catch (Exception e) {
                fail("Exception thrown: " + e.getMessage());
            }
        });
        t.start();
        try {
            Thread.sleep(1000); // Let it run for a short while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt();
    }

    public void testAcquireAndDoWork() {
        Thread t = new Thread(() -> {
            try {
                AffinityTestMain.acquireAndDoWork();
            } catch (Exception e) {
                fail("Exception thrown: " + e.getMessage());
            }
        });
        t.start();
        try {
            Thread.sleep(1000); // Let it run for a short while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt();
    }
}
