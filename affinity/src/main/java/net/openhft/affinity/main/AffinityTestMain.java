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
 * @author Tom Shercliff
 */
public class AffinityTestMain {

    public static void main(String[] args) {

        int cpus = 1;
        if (args.length == 0) {
            cpus = AffinityLock.cpuLayout().cpus() / 12;
        } else {
            cpus = Integer.valueOf(args[0]);
        }

        for (int i = 0; i < cpus; i++) {
            acquireAndDoWork();
        }
    }

    private static void acquireAndDoWork() {

        Thread t = new Thread(() -> {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM" + ".dd 'at' HH:mm:ss z");
            try (AffinityLock al = Affinity.acquireLock()) {
                String threadName = Thread.currentThread().getName();
                System.out.println("Thread (" + threadName + ") locked onto cpu " + al.cpuId());

                while (true) {
                    System.out.println(df.format(new Date()) + " - Thread (" + threadName + ") doing work on cpu " + al.cpuId() + ". IsAllocated = " + al.isAllocated() + ", isBound = " + al.isBound() + ". " + al.toString());

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
