/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
        Thread t = new Thread(() -> {
            try (AffinityLock ignored = acquireLockBasedOnLast()) {
                //noinspection ConstantValue
                assert ignored != null;
                r.run();
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
