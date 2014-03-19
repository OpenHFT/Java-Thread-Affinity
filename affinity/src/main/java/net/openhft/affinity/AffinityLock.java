/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.NoCpuLayout;
import net.openhft.affinity.impl.VanillaCpuLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This utility class support locking a thread to a single core, or reserving a whole core for a thread.
 *
 * @author peter.lawrey
 */
public class AffinityLock {
    private static final Logger LOGGER = Logger.getLogger(AffinityLock.class.getName());

    // TODO It seems like on virtualized platforms .availableProcessors() value can change at
    // TODO runtime. We should think about how to adopt to such change

    // Static fields and methods.
    public static final String AFFINITY_RESERVED = "affinity.reserved";

    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final long BASE_AFFINITY = AffinitySupport.getAffinity();
    public static final long RESERVED_AFFINITY = getReservedAffinity0();

    private static AffinityLock[] LOCKS;
    private static NavigableMap<Integer, AffinityLock[]> CORES; // set by cpuLayout()
    private static final AffinityLock NONE = new AffinityLock(-1, false, false);
    @NotNull
    private static CpuLayout cpuLayout = new NoCpuLayout(PROCESSORS);

    static {
        try {
            if (new File("/proc/cpuinfo").exists()) {
                cpuLayout(VanillaCpuLayout.fromCpuInfo());
            } else {
                LOCKS = new AffinityLock[PROCESSORS];
                CORES = new TreeMap<Integer, AffinityLock[]>();
                for (int i = 0; i < PROCESSORS; i++) {
                    AffinityLock al = LOCKS[i] = new AffinityLock(i, ((BASE_AFFINITY >> i) & 1) != 0, ((RESERVED_AFFINITY >> i) & 1) != 0);

                    final int layoutId = al.cpuId;
                    int logicalCpuId = coreForId(layoutId);
                    AffinityLock[] als = CORES.get(logicalCpuId);
                    if (als == null)
                        CORES.put(logicalCpuId, als = new AffinityLock[1]);
                    als[cpuLayout.threadId(layoutId)] = al;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to load /proc/cpuinfo", e);
        }
    }

    /**
     * Set the CPU layout for this machine.  CPUs which are not mentioned will be ignored.
     * <p></p>
     * Changing the layout will have no impact on thread which have already been assigned.
     * It only affects subsequent assignments.
     *
     * @param cpuLayout for this application to use for this machine.
     */
    public static void cpuLayout(@NotNull CpuLayout cpuLayout) {
        synchronized (AffinityLock.class) {
            if (cpuLayout.equals(AffinityLock.cpuLayout))
                return;
            AffinityLock.cpuLayout = cpuLayout;
//            System.out.println("Locks= " + cpuLayout.cpus());
            LOCKS = new AffinityLock[cpuLayout.cpus()];
            int threads = cpuLayout.threadsPerCore();
            CORES = new TreeMap<Integer, AffinityLock[]>();
            for (int i = 0; i < cpuLayout.cpus(); i++) {
                boolean base1 = ((BASE_AFFINITY >> i) & 1) != 0;
                boolean reservable1 = ((RESERVED_AFFINITY >> i) & 1) != 0;
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("cpu " + i + " base= " + base1 + " reservable= " + reservable1);
                AffinityLock al = LOCKS[i] = new AffinityLock(i, base1, reservable1);
                final int layoutId = al.cpuId;
                int logicalCpuId = coreForId(layoutId);
                AffinityLock[] als = CORES.get(logicalCpuId);
                if (als == null)
                    CORES.put(logicalCpuId, als = new AffinityLock[threads]);
                als[cpuLayout.threadId(layoutId)] = al;
            }
        }
    }

    /**
     * Translate a layout id into a logical cpu id.
     * <p></p>
     * This translation is perform so that regardless of how
     *
     * @param id
     * @return
     */

    private static int coreForId(int id) {
        return cpuLayout.socketId(id) * cpuLayout.coresPerSocket() + cpuLayout.coreId(id);
    }

    /**
     * @return The current CpuLayout for the application.
     */
    @NotNull
    public static CpuLayout cpuLayout() {
        return cpuLayout;
    }

    private static long getReservedAffinity0() {
        String reservedAffinity = System.getProperty(AFFINITY_RESERVED);
        if (reservedAffinity == null || reservedAffinity.trim().isEmpty()) {
            long reserverable = ((1 << PROCESSORS) - 1) ^ BASE_AFFINITY;
            if (reserverable == 0 && PROCESSORS > 1) {
                LOGGER.log(Level.INFO, "No isolated CPUs found, so assuming CPUs 1 to " + (PROCESSORS - 1) + " available.");
                return ((1 << PROCESSORS) - 2);
            }
            return reserverable;
        }
        return Long.parseLong(reservedAffinity, 16);
    }

    /**
     * Assign any free cpu to this thread.
     *
     * @return A handle for the current AffinityLock.
     */
    public static AffinityLock acquireLock() {
        return acquireLock(true);
    }

    /**
     * Assign any free core to this thread.
     * <p></p>
     * In reality, only one cpu is assigned, the rest of the threads for that core are reservable so they are not used.
     *
     * @return A handle for the current AffinityLock.
     */
    public static AffinityLock acquireCore() {
        return acquireCore(true);
    }

    /**
     * Assign a cpu which can be bound to the current thread or another thread.
     * <p></p>
     * This can be used for defining your thread layout centrally and passing the handle via dependency injection.
     *
     * @param bind if true, bind the current thread, if false, reserve a cpu which can be bound later.
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireLock(boolean bind) {
        return acquireLock(bind, -1, AffinityStrategies.ANY);
    }

    /**
     * Assign a core(and all its cpus) which can be bound to the current thread or another thread.
     * <p></p>
     * This can be used for defining your thread layout centrally and passing the handle via dependency injection.
     *
     * @param bind if true, bind the current thread, if false, reserve a cpu which can be bound later.
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireCore(boolean bind) {
        return acquireCore(bind, -1, AffinityStrategies.ANY);
    }

    private static AffinityLock acquireLock(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        synchronized (AffinityLock.class) {
            for (AffinityStrategy strategy : strategies) {
                // consider all processors except cpu 0 which is usually used by the OS.
                // if you have only one core, this library is not appropriate in any case.
                for (int i = LOCKS.length - 1; i > 0; i--) {
                    AffinityLock al = LOCKS[i];
                    if (al.canReserve() && (cpuId < 0 || strategy.matches(cpuId, al.cpuId))) {
                        al.assignCurrentThread(bind, false);
                        return al;
                    }
                }
            }
        }
        if (LOGGER.isLoggable(Level.WARNING))
            LOGGER.warning("No reservable CPU for " + Thread.currentThread());
        return AffinityLock.NONE;
    }

    private static AffinityLock acquireCore(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        synchronized (AffinityLock.class) {
            for (AffinityStrategy strategy : strategies) {
                LOOP:
                for (AffinityLock[] als : CORES.descendingMap().values()) {
                    for (AffinityLock al : als)
                        if (!al.canReserve() || !strategy.matches(cpuId, al.cpuId))
                            continue LOOP;

                    final AffinityLock al = als[0];
                    al.assignCurrentThread(bind, true);
                    return al;
                }
            }
        }
        if (LOGGER.isLoggable(Level.WARNING))
            LOGGER.warning("No reservable Core for " + Thread.currentThread());
        return acquireLock(bind, cpuId, strategies);
    }


    /**
     * @return All the current locks as a String.
     */
    @NotNull
    public static String dumpLocks() {
        return dumpLocks0(LOCKS);
    }

    @NotNull
    static String dumpLocks0(@NotNull AffinityLock[] locks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locks.length; i++) {
            AffinityLock al = locks[i];
            sb.append(i).append(": ");
            if (al.assignedThread != null)
                sb.append(al.assignedThread).append(" alive=").append(al.assignedThread.isAlive());
            else if (al.reservable)
                sb.append("Reserved for this application");
            else if (al.base)
                sb.append("General use CPU");
            else
                sb.append("CPU not available");
            sb.append('\n');
        }
        return sb.toString();
    }

    //// Non static fields and methods.
    private final int cpuId;
    private final boolean base;
    private final boolean reservable;
    boolean bound = false;
    @Nullable
    Thread assignedThread;

    AffinityLock(int cpuId, boolean base, boolean reservable) {
        this.cpuId = cpuId;
        this.base = base;
        this.reservable = reservable;
    }

    /**
     * Assigning the current thread has a side effect of preventing the lock being used again until it is released.
     *
     * @param bind      whether to bind the thread as well
     * @param wholeCore whether to reserve all the thread in the same core.
     */
    private void assignCurrentThread(boolean bind, boolean wholeCore) {
        assignedThread = Thread.currentThread();
        if (bind)
            bind(wholeCore);
    }

    /**
     * Bind the current thread to this reservable lock.
     */
    public void bind() {
        bind(false);
    }

    /**
     * Bind the current thread to this reservable lock.
     *
     * @param wholeCore if true, also reserve the whole core.
     */
    public void bind(boolean wholeCore) {
        if (bound && assignedThread != null && assignedThread.isAlive())
            throw new IllegalStateException("cpu " + cpuId + " already bound to " + assignedThread);

        if (wholeCore) {
            int core = coreForId(cpuId);
            for (AffinityLock al : CORES.get(core)) {
                if (bound && al.assignedThread != null && al.assignedThread.isAlive()) {
                    LOGGER.severe("cpu " + al.cpuId + " already bound to " + al.assignedThread);
                } else {
                    al.bound = true;
                    al.assignedThread = Thread.currentThread();
                }
            }
            if (LOGGER.isLoggable(Level.INFO)) {
                StringBuilder sb = new StringBuilder().append("Assigning core ").append(core);
                String sep = ": cpus ";
                for (AffinityLock al : CORES.get(core)) {
                    sb.append(sep).append(al.cpuId);
                    sep = ", ";
                }
                sb.append(" to ").append(assignedThread);
                LOGGER.info(sb.toString());
            }
        } else if (cpuId >= 0) {
            bound = true;
            assignedThread = Thread.currentThread();
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.info("Assigning cpu " + cpuId + " to " + assignedThread);
        }
        if (cpuId >= 0)
            AffinitySupport.setAffinity(1L << cpuId);
    }

    private boolean canReserve() {
        if (!reservable) return false;
        if (assignedThread != null) {
            if (assignedThread.isAlive()) return false;
            LOGGER.severe("Lock assigned to " + assignedThread + " but this thread is dead.");
        }
        return true;
    }

    /**
     * Give another affinity lock relative to this one based on a list of strategies.
     * <p></p>
     * The strategies are evaluated in order to (like a search path) to find the next appropriate thread.
     * If ANY is not the last strategy, a warning is logged and no cpu is assigned (leaving the OS to choose)
     *
     * @param strategies To determine if you want the same/different core/socket.
     * @return A matching AffinityLock.
     */
    public AffinityLock acquireLock(AffinityStrategy... strategies) {
        return acquireLock(false, cpuId, strategies);
    }

    /**
     * Release the current AffinityLock which can be discarded.
     */
    public void release() {
        Thread t = Thread.currentThread();
        synchronized (AffinityLock.class) {
            for (AffinityLock al : LOCKS) {
                Thread at = al.assignedThread;
                if (at == t) {
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.info("Releasing cpu " + al.cpuId + " from " + t);
                    al.assignedThread = null;
                    al.bound = false;
                } else if (at != null && !at.isAlive()) {
                    LOGGER.warning("Releasing cpu " + al.cpuId + " from " + t + " as it is not alive.");
                    al.assignedThread = null;
                    al.bound = false;
                }
            }
        }
        AffinitySupport.setAffinity(BASE_AFFINITY);
    }

    @Override
    protected void finalize() throws Throwable {
        if (reservable) {
            LOGGER.warning("Affinity lock for " + assignedThread + " was discarded rather than release()d in a controlled manner.");
            release();
        }
        super.finalize();
    }

    /**
     * @return unique id for this CPI or -1 if not allocated.
     */
    public int cpuId() {
        return cpuId;
    }

    /**
     * @return Was a cpu found to bind this lock to.
     */
    public boolean isAllocated() {
        return cpuId >= 0;
    }

    /**
     * @return Has this AffinityLock been bound?
     */
    public boolean isBound() {
        return bound;
    }
}
