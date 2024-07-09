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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadFactory;

/**
 * This is a ThreadFactory which assigns threads based the strategies provided.
 * <p>
 * If no strategies are provided AffinityStrategies.ANY is used.
 *
 * @author peter.lawrey
 */
public class AffinityThreadFactory implements ThreadFactory {
    // Name prefix for the threads created by this factory
    private final String name;

    // Flag to indicate if the threads created should be daemon threads
    private final boolean daemon;

    // Array of AffinityStrategy used to determine thread affinity
    @NotNull
    private final AffinityStrategy[] strategies;

    // The last acquired AffinityLock to maintain affinity across thread creations
    @Nullable
    private AffinityLock lastAffinityLock = null;

    // Counter to generate unique thread names
    private int id = 1;

    /**
     * Constructs an AffinityThreadFactory with the specified name and strategies.
     * Threads created by this factory will be daemon threads by default.
     *
     * @param name       the name prefix for the threads created by this factory
     * @param strategies the strategies used to determine thread affinity
     */
    public AffinityThreadFactory(String name, AffinityStrategy... strategies) {
        this(name, true, strategies);
    }

    /**
     * Constructs an AffinityThreadFactory with the specified name, daemon flag, and strategies.
     *
     * @param name       the name prefix for the threads created by this factory
     * @param daemon     if true, threads created by this factory will be daemon threads
     * @param strategies the strategies used to determine thread affinity
     */
    public AffinityThreadFactory(String name, boolean daemon, @NotNull AffinityStrategy... strategies) {
        this.name = name;
        this.daemon = daemon;
        this.strategies = strategies.length == 0 ? new AffinityStrategy[]{AffinityStrategies.ANY} : strategies;
    }

    /**
     * Creates a new Thread that will execute the given Runnable.
     * The thread will have a name based on the factory's name prefix and an incrementing ID,
     * and it will have its CPU affinity set according to the provided strategies.
     *
     * @param r the Runnable to be executed by the new thread
     * @return the newly created Thread
     */
    @NotNull
    @Override
    public synchronized Thread newThread(@NotNull final Runnable r) {
        // Generate a unique thread name
        String name2 = id <= 1 ? name : (name + '-' + id);
        id++;

        // Create a new thread with the specified name
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Acquire and bind the affinity lock before running the Runnable
                try (AffinityLock ignored = acquireLockBasedOnLast()) {
                    assert ignored != null;
                    r.run();
                }
            }
        }, name2);

        // Set the daemon flag for the new thread
        t.setDaemon(daemon);
        return t;
    }

    /**
     * Acquires an AffinityLock based on the last acquired lock and the specified strategies.
     * Binds the acquired lock to a CPU.
     *
     * @return the acquired and bound AffinityLock
     */
    synchronized AffinityLock acquireLockBasedOnLast() {
        // Acquire a new AffinityLock based on the last lock and strategies
        AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock(false) : lastAffinityLock.acquireLock(strategies);

        // Bind the acquired lock to a CPU
        al.bind();

        // Update the lastAffinityLock if a valid CPU was assigned
        if (al.cpuId() >= 0)
            lastAffinityLock = al;

        return al;
    }
}
