/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

/**
 * @author peter.lawrey
 */
public final class AffinitySupportMain {
    private AffinitySupportMain() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void main(String... args) {
        AffinityLock al = AffinityLock.acquireLock();
        try {
            new Thread(new SleepRunnable(), "reader").start();
            new Thread(new SleepRunnable(), "writer").start();
            new Thread(new SleepRunnable(), "engine").start();
        } finally {
            al.release();
        }
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
    }

    private static class SleepRunnable implements Runnable {
        @Override
        public void run() {
            AffinityLock al = AffinityLock.acquireLock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                al.release();
            }
        }
    }
}
