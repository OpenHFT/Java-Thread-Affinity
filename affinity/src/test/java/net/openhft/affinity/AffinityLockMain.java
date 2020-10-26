/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.affinity;

/**
 * @author peter.lawrey
 */
public final class AffinityLockMain {
    private AffinityLockMain() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void main(String... args) throws InterruptedException {
        AffinityLock al = AffinityLock.acquireLock();
        try {
            new Thread(new SleepRunnable(), "reader").start();
            new Thread(new SleepRunnable(), "writer").start();
            Thread.sleep(200);
        } finally {
            al.release();
        }
        new Thread(new SleepRunnable(), "engine").start();

        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
    }

    private static class SleepRunnable implements Runnable {
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
