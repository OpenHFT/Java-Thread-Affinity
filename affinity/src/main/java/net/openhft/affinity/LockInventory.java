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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NavigableMap;
import java.util.TreeMap;

class LockInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockInventory.class);
    /**
     * The locks belonging to physical cores. Since a physical core can host multiple logical cores
     * the relationship is one to many.
     */
    private final NavigableMap<Integer, AffinityLock[]> physicalCoreLocks = new TreeMap<Integer, AffinityLock[]>();
    private CpuLayout cpuLayout;
    /**
     * The lock belonging to each logical core. 1-to-1 relationship
     */
    private AffinityLock[] logicalCoreLocks;

    public LockInventory(CpuLayout cpuLayout) {
        set(cpuLayout);
    }

    public static String dumpLocks(@NotNull AffinityLock[] locks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locks.length; i++) {
            AffinityLock al = locks[i];
            sb.append(i).append(": ");
            sb.append(al.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    public final synchronized CpuLayout getCpuLayout() {
        return cpuLayout;
    }

    public final synchronized void set(CpuLayout cpuLayout) {
        if (cpuLayout.equals(this.cpuLayout)) {
            return;
        }
        reset(cpuLayout);
        for (int i = 0; i < cpuLayout.cpus(); i++) {
            final boolean base = AffinityLock.BASE_AFFINITY.get(i);
            final boolean reservable = AffinityLock.RESERVED_AFFINITY.get(i);

            LOGGER.trace("cpu " + i + " base={} reservable= {}", i, base, reservable);
            AffinityLock lock = logicalCoreLocks[i] = newLock(i, base, reservable);

            int layoutId = lock.cpuId();
            int physicalCore = toPhysicalCore(layoutId);
            AffinityLock[] locks = physicalCoreLocks.get(physicalCore);
            if (locks == null) {
                physicalCoreLocks.put(physicalCore, locks = new AffinityLock[cpuLayout.threadsPerCore()]);
            }
            locks[cpuLayout.threadId(layoutId)] = lock;
        }
    }

    public final synchronized AffinityLock acquireLock(boolean bind, int cpuId, AffinityStrategy... strategies) {
        for (AffinityStrategy strategy : strategies) {
            // consider all processors except cpu 0 which is usually used by the OS.
            // if you have only one core, this library is not appropriate in any case.
            for (int i = logicalCoreLocks.length - 1; i > 0; i--) {
                AffinityLock al = logicalCoreLocks[i];
                if (al.canReserve() && (cpuId < 0 || strategy.matches(cpuId, al.cpuId()))) {
                    al.assignCurrentThread(bind, false);
                    return al;
                }
            }
        }

        LOGGER.warn("No reservable CPU for {}", Thread.currentThread());

        return newLock(-1, false, false);
    }

    public final synchronized AffinityLock acquireCore(boolean bind, int cpuId, AffinityStrategy... strategies) {
        for (AffinityStrategy strategy : strategies) {
            LOOP:
            for (AffinityLock[] als : physicalCoreLocks.descendingMap().values()) {
                for (AffinityLock al : als)
                    if (!al.canReserve() || !strategy.matches(cpuId, al.cpuId()))
                        continue LOOP;

                final AffinityLock al = als[0];
                al.assignCurrentThread(bind, true);
                return al;
            }
        }

        LOGGER.warn("No reservable Core for {}", Thread.currentThread());

        return acquireLock(bind, cpuId, strategies);
    }

    public final synchronized void bindWholeCore(int logicalCoreID) {
        if (logicalCoreID < 0) {
            LOGGER.warn("Can't bind core since it was not possible to reserve it!");
            return;
        }

        int core = toPhysicalCore(logicalCoreID);
        for (AffinityLock al : physicalCoreLocks.get(core)) {
            if (al.isBound() && al.assignedThread != null && al.assignedThread.isAlive()) {
                LOGGER.warn("cpu {} already bound to {}", al.cpuId(), al.assignedThread);

            } else {
                al.bound = true;
                al.assignedThread = Thread.currentThread();
            }
        }

        if (LOGGER.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder().append("Assigning core ").append(core);
            String sep = ": cpus ";
            for (AffinityLock al : physicalCoreLocks.get(core)) {
                sb.append(sep).append(al.cpuId());
                sep = ", ";
            }
            sb.append(" to ").append(Thread.currentThread());
            LOGGER.info(sb.toString());
        }
    }

    public final synchronized void release() {
        Thread t = Thread.currentThread();
        for (AffinityLock al : logicalCoreLocks) {
            Thread at = al.assignedThread;
            if (at == t) {
                LOGGER.info("Releasing cpu {} from {}", al.cpuId(), t);
                al.assignedThread = null;
                al.bound = false;

            } else if (at != null && !at.isAlive()) {
                LOGGER.warn("Releasing cpu {} from {} as it is not alive.", al.cpuId(), t);
                al.assignedThread = null;
                al.bound = false;
            }
        }
        Affinity.setAffinity(AffinityLock.BASE_AFFINITY);
    }

    public final synchronized String dumpLocks() {
        return dumpLocks(logicalCoreLocks);
    }

    protected AffinityLock newLock(int cpuId, boolean base, boolean reservable) {
        return new AffinityLock(cpuId, base, reservable, this);
    }

    private void reset(CpuLayout cpuLayout) {
        this.cpuLayout = cpuLayout;
        this.logicalCoreLocks = new AffinityLock[cpuLayout.cpus()];
        this.physicalCoreLocks.clear();
    }

    private int toPhysicalCore(int layoutId) {
        return cpuLayout.socketId(layoutId) * cpuLayout.coresPerSocket() + cpuLayout.coreId(layoutId);
    }
}