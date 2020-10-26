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

import static net.openhft.affinity.AffinityStrategies.*;

/**
 * @author peter.lawrey
 */
public final class AffinityLockBindMain {
    private AffinityLockBindMain() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void main(String... args) throws InterruptedException {
        AffinityLock al = AffinityLock.acquireLock();
        try {
            // find a cpu on a different socket, otherwise a different core.
            AffinityLock readerLock = al.acquireLock(DIFFERENT_SOCKET, DIFFERENT_CORE);
            new Thread(new SleepRunnable(readerLock, false), "reader").start();

            // find a cpu on the same core, or the same socket, or any free cpu.
            AffinityLock writerLock = readerLock.acquireLock(SAME_CORE, SAME_SOCKET, ANY);
            new Thread(new SleepRunnable(writerLock, false), "writer").start();

            Thread.sleep(200);
        } finally {
            al.release();
        }

        // allocate a whole core to the engine so it doesn't have to compete for resources.
        al = AffinityLock.acquireCore(false);
        new Thread(new SleepRunnable(al, true), "engine").start();

        Thread.sleep(200);
        System.out.println("\nThe assignment of CPUs is\n" + AffinityLock.dumpLocks());
    }

    static class SleepRunnable implements Runnable {
        private final AffinityLock affinityLock;
        private final boolean wholeCore;

        SleepRunnable(AffinityLock affinityLock, boolean wholeCore) {
            this.affinityLock = affinityLock;
            this.wholeCore = wholeCore;
        }

        public void run() {
            affinityLock.bind(wholeCore);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                affinityLock.release();
            }
        }
    }
}
