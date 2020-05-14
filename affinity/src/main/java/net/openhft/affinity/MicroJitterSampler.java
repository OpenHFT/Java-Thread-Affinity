/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import java.io.PrintStream;
/* e.g.
Windows 10 i7-4770 laptop
After 1845 seconds, the average per hour was
2us	2435969
3us	548812
4us	508041
6us	60320
8us	25374
10us	1832324
14us	2089216
20us	391901
30us	16063
40us	6440
60us	2617
80us	1487
100us	1241
140us	826
200us	2108
300us	601
400us	159
600us	129
800us	215
1ms	155
2ms	229
5ms	24
10ms	38
20ms	32
 */

/**
 * User: peter.lawrey Date: 30/06/13 Time: 13:13
 */
public class MicroJitterSampler {

    private static final long[] DELAY = {
            2 * 1000, 3 * 1000, 4 * 1000, 6 * 1000, 8 * 1000, 10 * 1000, 14 * 1000,
            20 * 1000, 30 * 1000, 40 * 1000, 60 * 1000, 80 * 1000, 100 * 1000, 140 * 1000,
            200 * 1000, 300 * 1000, 400 * 1000, 600 * 1000, 800 * 1000, 1000 * 1000,
            2 * 1000 * 1000, 5 * 1000 * 1000, 10 * 1000 * 1000,
            20 * 1000 * 1000, 50 * 1000 * 1000, 100 * 1000 * 1000
    };
    private static final double UTIL = Double.parseDouble(System.getProperty("util", "50"));
    //    static final int CPU = Integer.getInteger("cpu", 0);
    private final int[] count = new int[DELAY.length];
    private long totalTime = 0;

    public static void main(String... ignored) throws InterruptedException {
        // AffinityLock al = AffinityLock.acquireLock();

        // warmup.
        new MicroJitterSampler().sample(1000 * 1000 * 1000);

        MicroJitterSampler microJitterSampler = new MicroJitterSampler();
        while (!Thread.currentThread().isInterrupted()) {
            if (UTIL >= 100) {
                microJitterSampler.sample(30L * 1000 * 1000 * 1000);
            } else {
                long sampleLength = (long) ((1 / (1 - UTIL / 100) - 1) * 1000 * 1000);
                for (int i = 0; i < 30 * 1000; i += 2) {
                    microJitterSampler.sample(sampleLength);
                    //noinspection BusyWait
                    Thread.sleep(1);
                }
            }

            microJitterSampler.print(System.out);
        }
    }

    private static String asString(long timeNS) {
        return timeNS < 1000 ? timeNS + "ns" :
                timeNS < 1000000 ? timeNS / 1000 + "us" :
                        timeNS < 1000000000 ? timeNS / 1000000 + "ms" :
                                timeNS / 1000000000 + "sec";
    }

    void sample(long intervalNS) {
        long prev = System.nanoTime();
        long end = prev + intervalNS;
        long now;
        do {
            now = System.nanoTime();
            long time = now - prev;
            if (time >= DELAY[0]) {
                int i;
                for (i = 1; i < DELAY.length; i++)
                    if (time < DELAY[i])
                        break;
                count[i - 1]++;
            }
            prev = now;
        } while (now < end);
        totalTime += intervalNS;
    }

    void print(PrintStream ps) {
        ps.println("After " + totalTime / 1000000000 + " seconds, the average per hour was");
        for (int i = 0; i < DELAY.length; i++) {
            if (count[i] < 1) continue;
            long countPerHour = (long) Math.ceil(count[i] * 3600e9 / totalTime);
            ps.println(asString(DELAY[i]) + '\t' + countPerHour);
        }
        ps.println();
    }
}
