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

import net.openhft.affinity.Affinity;
import net.openhft.affinity.AffinityLock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Tom Shercliff
 */
public class AffinityTestMain {

    private static final long DEFAULT_WORK_SLEEP_MILLIS = 10_000L;
    private static volatile long workSleepMillis = DEFAULT_WORK_SLEEP_MILLIS;

    public static void main(String[] args) {

        int cpus;
        if (args.length == 0) {
            cpus = AffinityLock.cpuLayout().cpus() / 12;
        } else {
            cpus = Integer.parseInt(args[0]);
        }

        for (int i = 0; i < cpus; i++) {
            acquireAndDoWork();
        }
    }

    private static void acquireAndDoWork() {
        createWorkerThread(Affinity::acquireLock, System.out::println).start();
    }

    static Thread createWorkerThread(Supplier<AffinityLock> lockSupplier, Consumer<String> output) {
        Thread t = new Thread(() -> {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
            try (AffinityLock al = lockSupplier.get()) {
                String threadName = Thread.currentThread().getName();
                output.accept("Thread (" + threadName + ") locked onto cpu " + al.cpuId());

                while (true) {
                    output.accept(df.format(new Date()) + " - Thread (" + threadName + ") doing work on cpu " + al.cpuId() + ". IsAllocated = " + al.isAllocated() + ", isBound = " + al.isBound() + ". " + al);

                    try {
                        //noinspection BusyWait
                        Thread.sleep(workSleepMillis);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        output.accept("Thread interrupted; exiting work loop");
                        break;
                    }
                }
            }
        });
        return t;
    }

    static void setWorkSleepMillisForTests(long millis) {
        workSleepMillis = millis;
    }

    static long getWorkSleepMillisForTests() {
        return workSleepMillis;
    }

    static void resetWorkSleepMillisForTests() {
        workSleepMillis = DEFAULT_WORK_SLEEP_MILLIS;
    }
}
