/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
                AffinityLock al = lastAffinityLock == null ? AffinityLock.acquireLock() : lastAffinityLock.acquireLock(strategies);
                try {
                    if (al.cpuId() >= 0)
                        lastAffinityLock = al;
                    r.run();
                } finally {
                    al.release();
                }
            }
        }, name2);
        t.setDaemon(daemon);
        return t;
    }
}
