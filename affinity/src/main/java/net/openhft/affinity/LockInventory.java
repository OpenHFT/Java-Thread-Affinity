package net.openhft.affinity;

import org.jetbrains.annotations.NotNull;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class LockInventory {

    private static final Logger LOGGER = Logger.getLogger(LockInventory.class.getName());

    private CpuLayout cpuLayout;

    /**
     * The lock belonging to each logical core. 1-to-1 relationship
     */
    private AffinityLock[] logicalCoreLocks;

    /**
     * The locks belonging to physical cores. Since a physical core can host multiple logical cores
     * the relationship is one to many.
     */
    private final NavigableMap<Integer, AffinityLock[]> physicalCoreLocks = new TreeMap<Integer, AffinityLock[]>();

    public LockInventory(CpuLayout cpuLayout) {
        set(cpuLayout);
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
            boolean base = ((AffinityLock.BASE_AFFINITY >> i) & 1) != 0;
            boolean reservable = ((AffinityLock.RESERVED_AFFINITY >> i) & 1) != 0;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("cpu " + i + " base= " + base + " reservable= " + reservable);
            }
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
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning("No reservable CPU for " + Thread.currentThread());
        }
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
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.warning("No reservable Core for " + Thread.currentThread());
        }
        return acquireLock(bind, cpuId, strategies);
    }

    public final synchronized void bindWholeCore(int logicalCoreID) {
        if (logicalCoreID < 0) {
            LOGGER.warning("Can't bind core since it was not possible to reserve it!");
            return;
        }

        int core = toPhysicalCore(logicalCoreID);
        for (AffinityLock al : physicalCoreLocks.get(core)) {
            if (al.isBound() && al.assignedThread != null && al.assignedThread.isAlive()) {
                LOGGER.severe("cpu " + al.cpuId() + " already bound to " + al.assignedThread);
            } else {
                al.bound = true;
                al.assignedThread = Thread.currentThread();
            }
        }

        if (LOGGER.isLoggable(Level.INFO)) {
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
                if (LOGGER.isLoggable(Level.INFO))
                    LOGGER.info("Releasing cpu " + al.cpuId() + " from " + t);
                al.assignedThread = null;
                al.bound = false;
            } else if (at != null && !at.isAlive()) {
                LOGGER.warning("Releasing cpu " + al.cpuId() + " from " + t + " as it is not alive.");
                al.assignedThread = null;
                al.bound = false;
            }
        }
        AffinitySupport.setAffinity(AffinityLock.BASE_AFFINITY);
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
}