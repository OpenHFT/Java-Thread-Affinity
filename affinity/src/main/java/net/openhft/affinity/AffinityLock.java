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

import net.openhft.affinity.impl.NoCpuLayout;
import net.openhft.affinity.impl.VanillaCpuLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.util.Arrays;
import java.util.BitSet;

/**
 * This utility class support locking a thread to a single core, or reserving a whole core for a
 * thread.
 *
 * @author peter.lawrey
 */
public class AffinityLock implements Closeable {
    // Static fields and methods.
    public static final String AFFINITY_RESERVED = "affinity.reserved";
    // TODO It seems like on virtualized platforms .availableProcessors() value can change at
    // TODO runtime. We should think about how to adopt to such change
    public static final int PROCESSORS;

    public static final BitSet BASE_AFFINITY;
    public static final BitSet RESERVED_AFFINITY;
    static final int ANY_CPU = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(AffinityLock.class);
    private static final LockInventory LOCK_INVENTORY;

    static {
        int processors = Runtime.getRuntime().availableProcessors();
        VanillaCpuLayout cpuLayout = null;
        try {
            if (new File("/proc/cpuinfo").exists()) {
                cpuLayout = VanillaCpuLayout.fromCpuInfo();
                processors = cpuLayout.cpus();
            }
        } catch (Throwable e) {
            LOGGER.warn("Unable to load /proc/cpuinfo", e);
        }
        PROCESSORS = processors;
        BASE_AFFINITY = Affinity.getAffinity();
        RESERVED_AFFINITY = getReservedAffinity0();
        LOCK_INVENTORY = new LockInventory(cpuLayout == null ? new NoCpuLayout(PROCESSORS) : cpuLayout);
    }

    /**
     * Logical ID of the CPU to which this lock belongs to.
     */
    private final int cpuId;
    /**
     * CPU to which this lock belongs to is of general use.
     */
    private final boolean base;
    /**
     * CPU to which this lock belongs to is reservable.
     */
    private final boolean reservable;
    /**
     * An inventory build from the CPU layout which keeps track of the various locks belonging to
     * each CPU.
     */
    private final LockInventory lockInventory;
    boolean bound = false;
    @Nullable
    Thread assignedThread;
    Throwable boundHere;
    private boolean resetAffinity = true;

    AffinityLock(int cpuId, boolean base, boolean reservable, LockInventory lockInventory) {
        this.lockInventory = lockInventory;
        this.cpuId = cpuId;
        this.base = base;
        this.reservable = reservable;
    }

    /**
     * Set the CPU layout for this machine.  CPUs which are not mentioned will be ignored. <p>
     * Changing the layout will have no impact on thread which have already been assigned. It only
     * affects subsequent assignments.
     *
     * @param cpuLayout for this application to use for this machine.
     */
    public static void cpuLayout(@NotNull CpuLayout cpuLayout) {
        LOCK_INVENTORY.set(cpuLayout);
    }

    /**
     * @return The current CpuLayout for the application.
     */
    @NotNull
    public static CpuLayout cpuLayout() {
        return LOCK_INVENTORY.getCpuLayout();
    }

    private static BitSet getReservedAffinity0() {
        String reservedAffinity = System.getProperty(AFFINITY_RESERVED);
        if (BASE_AFFINITY != null && (reservedAffinity == null || reservedAffinity.trim().isEmpty())) {
            BitSet reserverable = new BitSet(PROCESSORS);
            reserverable.set(1, PROCESSORS, true);
            reserverable.andNot(BASE_AFFINITY);
            if (reserverable.isEmpty() && PROCESSORS > 1) {
                LoggerFactory.getLogger(AffinityLock.class).info("No isolated CPUs found, so assuming CPUs 1 to {} available.", (PROCESSORS - 1));
                // make all but first CPUs available
                reserverable.set(1, PROCESSORS);
                return reserverable;
            }
            return reserverable;
        }

        reservedAffinity = reservedAffinity.trim();
        long[] longs = new long[1 + (reservedAffinity.length() - 1) / 16];
        int end = reservedAffinity.length();
        for(int i = 0; i < longs.length ; i++) {
            int begin = Math.max(0, end - 16);
            longs[i] = Long.parseLong(reservedAffinity.substring(begin, end), 16);
            end = begin;
        }
        return BitSet.valueOf(longs);
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
     * Assign any free core to this thread. <p> In reality, only one cpu is assigned, the rest of
     * the threads for that core are reservable so they are not used.
     *
     * @return A handle for the current AffinityLock.
     */
    public static AffinityLock acquireCore() {
        return acquireCore(true);
    }

    /**
     * Assign a cpu which can be bound to the current thread or another thread. <p> This can be used
     * for defining your thread layout centrally and passing the handle via dependency injection.
     *
     * @param bind if true, bind the current thread, if false, reserve a cpu which can be bound
     *             later.
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireLock(boolean bind) {
        return acquireLock(bind, ANY_CPU, AffinityStrategies.ANY);
    }

    /**
     * Assign a cpu which can be bound to the current thread or another thread. <p> This can be used
     * for defining your thread layout centrally and passing the handle via dependency injection.
     *
     * @param cpuId the CPU id to bind to
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireLock(int cpuId) {
        return acquireLock(true, cpuId, AffinityStrategies.ANY);
    }

    /**
     * Assign a cpu which can be bound to the current thread or another thread
     * Caller passes in an explicit set of preferred CPUs
     * The first available CPU is used, and the lock returned
     * If all CPUs in the set are unavailable then no lock is obtained
     *
     * @param cpus the array of available CPUs to bind to
     * @return A handle for an affinity lock, or nolock if no available CPU in the array
     */
    public static AffinityLock acquireLock(int[] cpus) {
        for( int cpu : cpus )
        {
            AffinityLock lock = tryAcquireLock(true, cpu);
            if(lock != null)
            {
                LOGGER.info("Acquired lock on CPU {}", cpu);
                return lock;
            }
        }

        LOGGER.warn("Failed to lock any CPU in explicit list " + Arrays.toString(cpus));
        return LOCK_INVENTORY.noLock();
    }

    /**
     * Allocate from the end.
     *
     * @param n positive number to allocate from.
     * @return the lock acquired
     */
    public static AffinityLock acquireLockLastMinus(int n) {
        return acquireLock(true, PROCESSORS - n, AffinityStrategies.ANY);
    }

    /**
     * Use a description to allocate a cpu.
     * <ul>
     *     <li>"N" being a positive integer means allocate this CPU,</li>
     *     <li>"last" or "last-N" means allocate from the end,</li>
     *     <li>"csv:1,2,5,6 eg means allocate first free core from the provided</li>
     *     <li>"any" means allow any</li>
     *     <li>"none" or null means</li>
     *     <li>"0" is not allowed</li>
     * </ul>
     *
     * @param desc of which cpu to pick
     * @return the AffinityLock obtained
     */
    public static AffinityLock acquireLock(String desc) {
        if (desc == null)
            return LOCK_INVENTORY.noLock();

        desc = desc.toLowerCase();
        int cpuId;
        if (desc.startsWith("last")) {
            String last = desc.substring(4);
            int lastN;
            if (last.isEmpty())
                lastN = 0;
            else
                try {
                    lastN = Integer.parseInt(last);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot parse '" + desc + "'", e);
                }
            if (lastN > 0)
                throw new IllegalArgumentException("Cannot parse '" + desc + "'");

            cpuId = PROCESSORS + lastN - 1;

        } else if (desc.startsWith("csv:")) {
            String content = desc.substring(4);
            int[] cpus = Arrays.asList(content.split(",")).stream()
                    .map(String::trim)
                    .mapToInt(Integer::parseInt).toArray();

            return acquireLock(cpus);

        } else if (desc.equals("none")) {
            return LOCK_INVENTORY.noLock();

        } else if (desc.equals("any")) {
            return acquireLock();

        } else {
            try {
                cpuId = Integer.parseInt(desc);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse '" + desc + "'", e);
            }
        }
        if (cpuId <= 0) {
            System.err.println("Cannot allocate 0 or negative cpuIds '" + desc + "'");
            return LOCK_INVENTORY.noLock();
        }
        return acquireLock(cpuId);
    }

    /**
     * Assign a core(and all its cpus) which can be bound to the current thread or another thread.
     * <p> This can be used for defining your thread layout centrally and passing the handle via
     * dependency injection.
     *
     * @param bind if true, bind the current thread, if false, reserve a cpu which can be bound
     *             later.
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireCore(boolean bind) {
        return acquireCore(bind, ANY_CPU, AffinityStrategies.ANY);
    }

    private static AffinityLock acquireLock(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        return LOCK_INVENTORY.acquireLock(bind, cpuId, strategies);
    }

    /**
     * Try to acquire a lock on the specified core
     * Returns lock if successful, or null if cpu cannot be acquired
     *
     * @param bind - if true, bind the current thread; if false, reserve a cpu which can be bound later
     * @param cpuId - the cpu to lock
     * @return - A handle to an affinity lock on success; null if failed to lock
     */
    private static AffinityLock tryAcquireLock(boolean bind, int cpuId) {
        return LOCK_INVENTORY.tryAcquireLock(bind, cpuId);
    }

    private static AffinityLock acquireCore(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        return LOCK_INVENTORY.acquireCore(bind, cpuId, strategies);
    }

    /**
     * @return All the current locks as a String.
     */
    @NotNull
    public static String dumpLocks() {
        return LOCK_INVENTORY.dumpLocks();
    }

    private static boolean areAssertionsEnabled() {
        boolean debug = false;
        assert debug = true;
        return debug;
    }

    /**
     * @return Whether to reset the affinity, false indicates the thread is about to die anyway.
     */
    public boolean resetAffinity() {
        return resetAffinity;
    }

    /**
     * @param resetAffinity Whether to reset the affinity, false indicates the thread is about to die anyway.
     * @return this
     */
    public AffinityLock resetAffinity(boolean resetAffinity) {
        this.resetAffinity = resetAffinity;
        return this;
    }

    /**
     * Assigning the current thread has a side effect of preventing the lock being used again until
     * it is released.
     *
     * @param bind      whether to bind the thread as well
     * @param wholeCore whether to reserve all the thread in the same core.
     */
    final void assignCurrentThread(boolean bind, boolean wholeCore) {
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

        if (areAssertionsEnabled())
            boundHere = new Throwable("Bound here");
        if (wholeCore) {
            lockInventory.bindWholeCore(cpuId);

        } else if (cpuId >= 0) {
            bound = true;
            assignedThread = Thread.currentThread();
            LOGGER.info("Assigning cpu {} to {} on thread id {}", cpuId, assignedThread, Affinity.getThreadId());
        }
        if (cpuId >= 0) {
            BitSet affinity = new BitSet();
            affinity.set(cpuId, true);
            Affinity.setAffinity(affinity);
        }
    }

    final boolean canReserve(boolean specified) {

        if (!specified && !reservable)
            return false;

        if (!LockCheck.isCpuFree(cpuId))
            return false;

        if (assignedThread != null) {
            if (assignedThread.isAlive()) {
                return false;
            }

            LOGGER.warn("Lock assigned to {} but this thread is dead.", assignedThread);
        }
        return true;
    }

    /**
     * Give another affinity lock relative to this one based on a list of strategies. <p> The
     * strategies are evaluated in order to (like a search path) to find the next appropriate
     * thread. If ANY is not the last strategy, a warning is logged and no cpu is assigned (leaving
     * the OS to choose)
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
        if (cpuId == ANY_CPU)
            return;
        // expensive if not actually used.
        boolean resetAffinity = this.resetAffinity;
        this.resetAffinity = true;
        lockInventory.release(resetAffinity);
    }

    @Override
    public void close() {
        release();
    }

    @Override
    protected void finalize() throws Throwable {
        if (bound) {
            LOGGER.warn("Affinity lock for " + assignedThread + " was discarded rather than release()d in a controlled manner.", boundHere);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (assignedThread != null)
            sb.append(assignedThread).append(" alive=").append(assignedThread.isAlive());
        else if (reservable)
            sb.append("Reserved for this application");
        else if (base)
            sb.append("General use CPU");
        else
            sb.append("CPU not available");
        return sb.toString();
    }
}
