/*
 * Copyright 2014 Higher Frequency Trading
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

package net.openhft.affinity;

import java.io.PrintStream;

/**
 * The MicroJitterSampler class samples microsecond-level jitter in the system's time measurements.
 * It uses a specified CPU and utilization level to perform the sampling and can operate in busy-wait or sleep mode.
 * The results are printed out showing the jitter counts at various delay levels.
 */
public class MicroJitterSampler {

    // Array of delay thresholds in nanoseconds
    private static final long[] DELAY = {
            2 * 1000, 3 * 1000, 4 * 1000, 6 * 1000, 8 * 1000, 10 * 1000, 14 * 1000,
            20 * 1000, 30 * 1000, 40 * 1000, 60 * 1000, 80 * 1000, 100 * 1000, 140 * 1000,
            200 * 1000, 300 * 1000, 400 * 1000, 600 * 1000, 800 * 1000, 1000 * 1000,
            2 * 1000 * 1000, 5 * 1000 * 1000, 10 * 1000 * 1000,
            20 * 1000 * 1000, 50 * 1000 * 1000, 100 * 1000 * 1000
    };
    // Utilization percentage from system property
    private static final double UTIL = Double.parseDouble(System.getProperty("util", "50"));
    // Busy-wait flag from system property
    private static final boolean BUSYWAIT = Boolean.parseBoolean(System.getProperty("busywait", "false"));
    // CPU to use from system property
    private static final String CPU = System.getProperty("cpu", "none");

    // Count of delays for each threshold
    final int[] count = new int[DELAY.length];
    // Total sampling time
    long totalTime = 0;

    /**
     * Pauses the thread for a short duration. Uses busy-wait if BUSYWAIT is true, otherwise sleeps for 1 millisecond.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    static void pause() throws InterruptedException {
        if (BUSYWAIT) {
            long now = System.nanoTime();
            while (System.nanoTime() - now < 1_000_000) ;
        } else {
            Thread.sleep(1);
        }
    }

    /**
     * Main method to run the MicroJitterSampler.
     *
     * @param ignored command line arguments (ignored)
     * @throws InterruptedException if the thread is interrupted
     */
    public static void main(String... ignored) throws InterruptedException {
        MicroJitterSampler sampler = new MicroJitterSampler();

        Thread t = new Thread(sampler::run);
        t.start();
        t.join();
    }

    /**
     * Performs a single sampling run based on the UTIL value.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    void once() throws InterruptedException {
        if (UTIL >= 100) {
            sample(30L * 1000 * 1000 * 1000);
        } else {
            long sampleLength = (long) ((1 / (1 - UTIL / 100) - 1) * 1000 * 1000);
            for (int i = 0; i < 30 * 1000; i += 2) {
                sample(sampleLength);
                //noinspection BusyWait
                pause();
            }
        }
    }

    /**
     * Runs the sampling loop, acquiring an AffinityLock and performing warmup before actual sampling.
     */
    public void run() {
        try (final AffinityLock lock = AffinityLock.acquireLock(CPU)) {
            assert lock != null;
            boolean first = true;
            System.out.println("Warming up...");
            while (!Thread.currentThread().isInterrupted()) {
                once();

                if (first) {
                    reset();
                    first = false;
                    System.out.println("Warmup complete. Running jitter tests...");
                    continue;
                }

                print(System.out);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Converts a time in nanoseconds to a human-readable string.
     *
     * @param timeNS the time in nanoseconds
     * @return the time as a string with appropriate units
     */
    static String asString(long timeNS) {
        return timeNS < 1000 ? timeNS + "ns" :
                timeNS < 1000000 ? timeNS / 1000 + "us" :
                        timeNS < 1000000000 ? timeNS / 1000000 + "ms" :
                                timeNS / 1000000000 + "sec";
    }

    /**
     * Resets the count array and total time.
     */
    void reset() {
        for (int i = 0; i < DELAY.length; ++i)
            count[i] = 0;
        totalTime = 0;
    }

    /**
     * Samples the jitter over a specified interval, updating the count array.
     *
     * @param intervalNS the sampling interval in nanoseconds
     */
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

    /**
     * Prints the jitter counts to the specified PrintStream.
     *
     * @param ps the PrintStream to print to
     */
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

/* e.g.
Ubuntu 20.04, Ryzen 5950X with an isolated CPU. (init 3) sudo cpupower -c {cpu} -g performance, run from command line
After 3600 seconds, the average per hour was
2us     2571
3us     2304
4us     376
6us     1
10us    1
20us    1
30us    1
40us    2
60us    1

Ubuntu 20.04, Ryzen 5950X with an isolated CPU. (init 5) sudo cpupower -c {cpu} -g performance, run from command line
After 3600 seconds, the average per hour was
2us	2157
3us	3444
4us	3654
6us	135
8us	4
14us	1
20us	1
40us	2
60us	1

Ubuntu 20.04, Ryzen 5950X with an isolated CPU. (init 5) sudo cpupower -c {cpu} -g performance, run from IntelliJ CE
After 7200 seconds, the average per hour was
2us	2189
3us	3341
4us	2335
6us	191
8us	4
14us	1
20us	1

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

On an Centos 7 machine with an isolated CPU.
After 2145 seconds, the average per hour was
2us	781271
3us	1212123
4us	13504
6us	489
8us	2
10us	3032577
14us	17475
20us	628
30us	645
40us	1301
60us	1217
80us	1306
100us	1526
140us	22

 */
