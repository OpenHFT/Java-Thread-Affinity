/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
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

/**
 * The AffinityTestMain class demonstrates how to use the Affinity library to lock threads onto specific CPUs.
 * It creates a number of threads based on the available CPUs and performs work while being locked onto a CPU.
 * <p>
 * If no arguments are provided, the number of CPUs used is one-twelfth of the available CPUs.
 * If an argument is provided, it specifies the number of CPUs to use.
 * <p>
 * Each thread prints out the current date and time along with its CPU allocation status every 10 seconds.
 * <p>
 * Usage: java AffinityTestMain [number_of_cpus]
 * </p>
 *
 * Author: Tom Shercliff
 */
public class AffinityTestMain {

    /**
     * The main method that starts the AffinityTestMain application.
     *
     * @param args command line arguments; if provided, the first argument specifies the number of CPUs to use
     */
    public static void main(String[] args) {
        int cpus = 1;

        // Determine the number of CPUs to use
        if (args.length == 0) {
            cpus = AffinityLock.cpuLayout().cpus() / 12;
        } else {
            cpus = Integer.valueOf(args[0]);
        }

        // Create and start a thread for each CPU
        for (int i = 0; i < cpus; i++) {
            acquireAndDoWork();
        }
    }

    /**
     * Acquires a lock on a CPU and starts a thread to perform work while locked onto that CPU.
     */
    static void acquireAndDoWork() {

        Thread t = new Thread(() -> {
            // Create a SimpleDateFormat to format the current date
            final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");

            // Acquire a lock on a CPU
            try (AffinityLock al = Affinity.acquireLock()) {
                String threadName = Thread.currentThread().getName();
                System.out.println("Thread (" + threadName + ") locked onto cpu " + al.cpuId());

                // Perform work while locked onto the CPU
                while (true) {
                    System.out.println(df.format(new Date()) + " - Thread (" + threadName + ") doing work on cpu " + al.cpuId() + ". IsAllocated = " + al.isAllocated() + ", isBound = " + al.isBound() + ". " + al.toString());

                    // Sleep for 10 seconds before printing again
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        //nothing
                    }
                }
            }
        });
        t.start();
    }
}
