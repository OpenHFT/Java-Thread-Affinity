/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import net.openhft.affinity.impl.NullAffinity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.NavigableMap;
import java.util.TreeMap;

import static net.openhft.affinity.Affinity.getAffinityImpl;

class LockInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockInventory.class);
    /**
     * The locks belonging to physical cores. Since a physical core can host multiple logical cores
     * the relationship is one to many.
     */
    private final NavigableMap<Integer, AffinityLock[]> physicalCoreLocks = new TreeMap<>();
    private CpuLayout cpuLayout;
    /**
     * The lock belonging to each logical core. 1-to-1 relationship
     */
    private AffinityLock[] logicalCoreLocks;

    private final Supplier<CpuCapabilitySnapshot> capabilities;
    private final Supplier<NumaLocality> locality;

    public LockInventory(CpuLayout cpuLayout) {
        this(cpuLayout, () -> {
            BitSet allowed = Affinity.getAffinity();
            return LinuxCpuCapabilities.read(allowed == null ? new BitSet() : allowed);
        }, () -> NumaLocality.read(Paths.get("/sys")));
    }

    LockInventory(CpuLayout cpuLayout, Supplier<CpuCapabilitySnapshot> capabilities) {
        this(cpuLayout, capabilities, NumaLocality::unknown);
    }

    LockInventory(CpuLayout cpuLayout, Supplier<CpuCapabilitySnapshot> capabilities, Supplier<NumaLocality> locality) {
        this.capabilities = capabilities;
        this.locality = locality;
        set(cpuLayout);
    }

    private boolean preferPerformance(int cpuId) {
        return isAnyCpu(cpuId) && System.getProperty("os.name", "").startsWith("Linux")
                && !Boolean.getBoolean("affinity.disablePerformanceCorePreference");
    }

    private boolean freePhysicalCore(int id) {
        for (AffinityLock sibling : physicalCoreLocks.get(toPhysicalCore(id)))
            if (!sibling.canReserve(false)) return false;
        return true;
    }

    private List<Integer> ordered(List<Integer> legacy, CpuCapabilitySnapshot snapshot) {
        PerformanceCpuOrder order = PerformanceCpuOrder.choose(legacy, snapshot, this::freePhysicalCore, locality.get());
        LOGGER.debug("Automatic CPU preference: {}; candidates={}", order.reason, order.ids);
        return order.ids;
    }

    private List<AffinityLock> logicalCandidates(int cpuId) {
        List<Integer> ids = new ArrayList<>();
        for (int i = logicalCoreLocks.length - 1; i > 0; i--)
            if (!preferPerformance(cpuId) || logicalCoreLocks[i].reservable()) ids.add(i);
        if (preferPerformance(cpuId)) ids = ordered(ids, capabilities.get());
        List<AffinityLock> result = new ArrayList<>();
        for (int id : ids) result.add(logicalCoreLocks[id]);
        return result;
    }

    private List<AffinityLock[]> coreCandidates(int cpuId) {
        List<AffinityLock[]> legacy = new ArrayList<>(physicalCoreLocks.descendingMap().values());
        if (!preferPerformance(cpuId)) return legacy;
        CpuCapabilitySnapshot snapshot = capabilities.get();
        List<Integer> ids = new ArrayList<>();
        Map<Integer, AffinityLock[]> groups = new TreeMap<>();
        boolean uniform = true;
        for (AffinityLock[] group : legacy) {
            boolean eligible = group.length > 0;
            for (AffinityLock lock : group)
                eligible &= lock.cpuId() > 0 && lock.reservable() && snapshot.cpus().containsKey(lock.cpuId());
            if (eligible) {
                CpuCapability.CoreType type = snapshot.cpus().get(group[0].cpuId()).coreType();
                for (AffinityLock lock : group)
                    uniform &= snapshot.cpus().get(lock.cpuId()).coreType() == type;
                ids.add(group[0].cpuId()); groups.put(group[0].cpuId(), group);
            }
        }
        List<AffinityLock[]> result = new ArrayList<>();
        if (!uniform) LOGGER.debug("Automatic core preference: legacy_order_conflicting_sibling_classes");
        for (int id : uniform ? ordered(ids, snapshot) : ids) result.add(groups.get(id));
        return result;
    }


    public static String dumpLocks(@NotNull AffinityLock[] locks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locks.length; i++) {
            AffinityLock al = locks[i];
            sb.append(i).append(": ");
            sb.append(al);
            sb.append('\n');
        }
        return sb.toString();
    }

    private static boolean anyStrategyMatches(final int cpuOne, final int cpuTwo, final AffinityStrategy[] strategies) {
        for (AffinityStrategy strategy : strategies) {
            if (strategy.matches(cpuOne, cpuTwo)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyCpu(final int cpuId) {
        return cpuId == AffinityLock.ANY_CPU;
    }

    /**
     * Update the lock for the current thread
     *
     * @param bind      Whether to also bind the thread to the core
     * @param al        The lock to update
     * @param wholeCore Whether to bind the whole core
     * @return true if the lock was acquired, false otherwise
     */
    private static boolean updateLockForCurrentThread(final boolean bind, final AffinityLock al, final boolean wholeCore) throws ClosedByInterruptException {
        try {
            if (LockCheck.updateCpu(al.cpuId(), wholeCore ? al.cpuId2() : 0)) {
                al.assignCurrentThread(bind, wholeCore);
                return true;
            }
        } catch (ClosedByInterruptException e) {
            throw e;

        } catch (IOException e) {
            LOGGER.info("Error occurred acquiring lock, trying another {}", String.valueOf(e));
        }
        return false;
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
            LOGGER.trace("cpu {} base={} reservable= {}", i, base, reservable);
            assert logicalCoreLocks != null;
            @SuppressWarnings("resource")
            AffinityLock lock = logicalCoreLocks[i] = newLock(i, cpuLayout.pair(i), base, reservable);

            int layoutId = lock.cpuId();
            int physicalCore = toPhysicalCore(layoutId);
            AffinityLock[] locks = physicalCoreLocks.get(physicalCore);
            if (locks == null) {
                physicalCoreLocks.put(physicalCore, locks = new AffinityLock[cpuLayout.threadsPerCore()]);
            }
            locks[cpuLayout.threadId(layoutId)] = lock;
        }
        shrink(physicalCoreLocks);
    }

    /**
     * If some CPUs are hyper-threaded, but not others, fix up the HT CPUs
     */
    private void shrink(NavigableMap<Integer, AffinityLock[]> physicalCoreLocks) {
        for (Map.Entry<Integer, AffinityLock[]> e : physicalCoreLocks.entrySet()) {
            final AffinityLock[] locks = e.getValue();
            for (int i = 0; i < locks.length; i++) {
                if (locks[i] == null) {
                    final AffinityLock[] locks2 = new AffinityLock[i];
                    System.arraycopy(locks, 0, locks2, 0, i);
                    physicalCoreLocks.put(e.getKey(), locks2);
                    break;
                }
            }
        }
    }

    public final synchronized AffinityLock acquireLock(boolean bind, int cpuId, AffinityStrategy... strategies) {
        if (getAffinityImpl() instanceof NullAffinity)
            return noLock();

        final boolean specificCpuRequested = !isAnyCpu(cpuId);
        try {
            if (specificCpuRequested && cpuId != 0) {
                if (cpuId > logicalCoreLocks.length) {
                    LOGGER.warn("Unable to acquire lock on CPU {} for thread {}, as not enough CPUs",
                            cpuId, Thread.currentThread());
                    return noLock();
                }

                final AffinityLock required = logicalCoreLocks[cpuId];
                if (required.canReserve(true)
                        && anyStrategyMatches(cpuId, cpuId, strategies)
                        && updateLockForCurrentThread(bind, required, false)) {
                    return required;
                }
                LOGGER.warn("Unable to acquire lock on CPU {} for thread {}, trying to find another CPU",
                        cpuId, Thread.currentThread());
            }

            List<AffinityLock> candidates = logicalCandidates(cpuId);
            for (AffinityStrategy strategy : strategies) {
                // consider all processors except cpu 0 which is usually used by the OS.
                // if you have only one core, this library is not appropriate in any case.
                for (AffinityLock al : candidates) {
                    if (al.canReserve(false)
                            && (isAnyCpu(cpuId) || strategy.matches(cpuId, al.cpuId()))
                            && updateLockForCurrentThread(bind, al, false)) {
                        return al;
                    }
                }
            }
        } catch (ClosedByInterruptException e) {
            Thread.currentThread().interrupt();
            return noLock();
        }

        LOGGER.warn("No reservable CPU for {}", Thread.currentThread());

        return noLock();
    }

    public final synchronized AffinityLock tryAcquireLock(boolean bind, int cpuId) {
        if (getAffinityImpl() instanceof NullAffinity)
            return null;
        if (cpuId > logicalCoreLocks.length)
            return null;
        final AffinityLock required = logicalCoreLocks[cpuId];
        try {
            if (required.canReserve(true)
                    && updateLockForCurrentThread(bind, required, false)) {
                return required;
            }
        } catch (ClosedByInterruptException e) {
            Thread.currentThread().interrupt();
            return noLock();
        }

        LOGGER.warn("Unable to acquire lock on CPU {} for thread {}, trying to find another CPU",
                cpuId, Thread.currentThread());

        return null;
    }

    public final synchronized AffinityLock acquireCore(boolean bind, int cpuId, AffinityStrategy... strategies) {
        List<AffinityLock[]> candidates = coreCandidates(cpuId);
        for (AffinityStrategy strategy : strategies) {
            LOOP:
            for (AffinityLock[] als : candidates) {
                for (AffinityLock al : als)
                    if (!al.canReserve(false) || !strategy.matches(cpuId, al.cpuId()))
                        continue LOOP;

                final AffinityLock al = als[0];
                try {
                    if (updateLockForCurrentThread(bind, al, true)) {
                        return al;
                    }
                } catch (ClosedByInterruptException e) {
                    Thread.currentThread().interrupt();
                    return noLock();
                }
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

    public final synchronized void release(boolean resetAffinity) {
        Thread t = Thread.currentThread();
        for (AffinityLock al : logicalCoreLocks) {
            Thread at = al.assignedThread;
            if (at == t) {
                releaseAffinityLock(t, al, "Releasing cpu {} from {}");
            } else if (at != null && !at.isAlive()) {
                releaseAffinityLock(t, al, "Releasing cpu {} from {} as it is not alive.");
            }
        }
        if (resetAffinity)
            Affinity.resetToBaseAffinity();
    }

    public final synchronized String dumpLocks() {
        return dumpLocks(logicalCoreLocks);
    }

    protected AffinityLock newLock(int cpuId, int cpuId2, boolean base, boolean reservable) {
        return new AffinityLock(cpuId, cpuId2, base, reservable, this);
    }

    private void reset(CpuLayout cpuLayout) {
        this.cpuLayout = cpuLayout;
        this.logicalCoreLocks = new AffinityLock[cpuLayout.cpus()];
        this.physicalCoreLocks.clear();
    }

    private int toPhysicalCore(int layoutId) {
        return cpuLayout.socketId(layoutId) * cpuLayout.coresPerSocket() + cpuLayout.coreId(layoutId);
    }

    private void releaseAffinityLock(final Thread t, final AffinityLock al, final String format) {
        LOGGER.info(format, al.cpuId(), t);
        al.assignedThread = null;
        al.bound = false;
        al.boundHere = null;

        LockCheck.releaseLock(al.cpuId());
    }

    public AffinityLock noLock() {
        return newLock(AffinityLock.ANY_CPU, 0, false, false);
    }
}
