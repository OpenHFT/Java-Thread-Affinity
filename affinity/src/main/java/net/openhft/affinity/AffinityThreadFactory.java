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
    private final String name;
    private final boolean daemon;
    @NotNull
    private final AffinityStrategy[] strategies;
    @Nullable
    private AffinityLock lastAffinityLock = null;
    private int id = 1;

    public AffinityThreadFactory(String name, AffinityStrategy... strategies) {
        this(name, true, strategies);
    }

    public AffinityThreadFactory(String name, boolean daemon, @NotNull AffinityStrategy... strategies) {
        this.name = name;
        this.daemon = daemon;
        this.strategies = strategies.length == 0 ? new AffinityStrategy[]{AffinityStrategies.ANY} : strategies;
    }

    @NotNull
    @Override
    public synchronized Thread newThread(@NotNull final Runnable r) {
        String name2 = id <= 1 ? name : (name + '-' + id);
        id++;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (AffinityLock ignored = acquireLockBasedOnLast()) {
                    r.run();
                }
            }
        }, name2);
        t.setDaemon(daemon);
        return t;
    }

    private synchronized AffinityLock acquireLockBasedOnLast() {
        AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock(false) : lastAffinityLock.acquireLock(strategies);
        al.bind();
        if (al.cpuId() >= 0)
            lastAffinityLock = al;
        return al;
    }
}
