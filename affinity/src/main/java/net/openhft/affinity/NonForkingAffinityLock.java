/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import net.openhft.affinity.impl.NoCpuLayout;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class NonForkingAffinityLock extends AffinityLock implements ThreadLifecycleListener {

    private static final Field GROUP_FIELD = makeThreadFieldModifiable("group");

    private static final Field TARGET_FIELD = makeThreadFieldModifiable("target");

    private static final LockInventory LOCK_INVENTORY = new LockInventory(new NoCpuLayout(PROCESSORS)) {
        @Override
        protected AffinityLock newLock(int cpuId, boolean base, boolean reservable) {
            return new NonForkingAffinityLock(cpuId, base, reservable, this);
        }
    };

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
     * <p>
     * In reality, only one cpu is assigned, the rest of the threads for that core are reservable so they are not used.
     *
     * @return A handle for the current AffinityLock.
     */
    public static AffinityLock acquireCore() {
        return acquireCore(true);
    }

    /**
     * Assign a cpu which can be bound to the current thread or another thread.
     * <p>
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
     * <p>
     * This can be used for defining your thread layout centrally and passing the handle via dependency injection.
     *
     * @param bind if true, bind the current thread, if false, reserve a cpu which can be bound later.
     * @return A handle for an affinity lock.
     */
    public static AffinityLock acquireCore(boolean bind) {
        return acquireCore(bind, -1, AffinityStrategies.ANY);
    }

    private static AffinityLock acquireLock(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        return LOCK_INVENTORY.acquireLock(bind, cpuId, strategies);
    }

    private static AffinityLock acquireCore(boolean bind, int cpuId, @NotNull AffinityStrategy... strategies) {
        return LOCK_INVENTORY.acquireCore(bind, cpuId, strategies);
    }

    /**
     * Set the CPU layout for this machine.  CPUs which are not mentioned will be ignored.
     * <p>
     * Changing the layout will have no impact on thread which have already been assigned.
     * It only affects subsequent assignments.
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

    /**
     * @return All the current locks as a String.
     */
    @NotNull
    public static String dumpLocks() {
        return LOCK_INVENTORY.dumpLocks();
    }

    NonForkingAffinityLock(int cpuId, boolean base, boolean reservable, LockInventory lockInventory) {
        super(cpuId, base, reservable, lockInventory);
    }

    @Override
    public void bind(boolean wholeCore) {
        super.bind(wholeCore);
        Thread thread = Thread.currentThread();
        changeGroupOfThread(thread, new ThreadTrackingGroup(thread.getThreadGroup(), this));
    }

    @Override
    public void release() {
        Thread thread = Thread.currentThread();
        changeGroupOfThread(thread, thread.getThreadGroup().getParent());
        super.release();
    }

    @Override
    public void started(Thread t) {
        wrapRunnableOfThread(t, this);
    }

    @Override
    public void startFailed(Thread t) {
    }

    @Override
    public void terminated(Thread t) {
    }

    private static Field makeThreadFieldModifiable(String fieldName) {
        try {
            Field field = Thread.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(Thread.class.getName() + " class doesn't have a " + fieldName + " field! Quite unexpected!");
        }
    }

    private static void changeGroupOfThread(Thread thread, ThreadGroup group) {
        try {
            GROUP_FIELD.set(thread, group);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed changing " + Thread.class.getName() + "'s the '" + GROUP_FIELD.getName() + "' field! Reason: " + e.getMessage());
        }
    }

    private static void wrapRunnableOfThread(Thread thread, final AffinityLock lock) {
        try {
            final Runnable originalRunnable = (Runnable) TARGET_FIELD.get(thread);
            TARGET_FIELD.set(
                    thread,
                    new Runnable() {
                        @Override
                        public void run() {
                            lock.release();
                            originalRunnable.run();
                        }
                    }
            );
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed wrapping " + Thread.class.getName() + "'s '" + TARGET_FIELD.getName() + "' field! Reason: " + e.getMessage());
        }
    }
}
